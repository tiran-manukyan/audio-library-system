package audiohub.service;

import audiohub.config.OutboxConfig;
import audiohub.dto.request.SongMetadataDto;
import audiohub.entity.OutboxEvent;
import audiohub.entity.OutboxEventType;
import audiohub.repository.OutboxRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherFacade {

    private final OutboxConfig outboxConfig;
    private final OutboxRepository outboxRepository;
    private final SongServiceClient songServiceClient;
    private final ObjectMapper objectMapper;

    @Async("outboxTaskExecutor")
    @Transactional
    public void processCreateEvent(long resourceId, SongMetadataDto metadata) {
        Set<Long> lockedIds = outboxRepository.lock(Set.of(resourceId));
        if (lockedIds.isEmpty()) {
            return;
        }

        log.debug("Immediate processing CREATE for resource ID: {}", resourceId);

        try {
            songServiceClient.createSongMetadata(resourceId, metadata);

            int removed = deleteOutboxEvents(
                    OutboxEventType.CREATE_METADATA,
                    Set.of(resourceId)
            );

            log.info("Immediate CREATE delivered for resource ID: {}, removed {} outbox events",
                    resourceId, removed);
        } catch (Exception ex) {
            if (isConflict409(ex)) {
                int removed = deleteOutboxEvents(
                        OutboxEventType.CREATE_METADATA,
                        Set.of(resourceId)
                );
                log.info("CREATE got 409 (already exists) for resource ID: {}, removed {} outbox events",
                        resourceId, removed);
                return;
            }

            String detailedError = extractDetailedError(ex);
            markAsFailed(OutboxEventType.CREATE_METADATA, Set.of(resourceId), detailedError);

            log.warn("Immediate CREATE failed for resource ID: {}. Error: {}", resourceId, detailedError);
        }
    }

    @Async("outboxTaskExecutor")
    @Transactional
    public void processDeleteEvents(Set<Long> resourceIds) {
        if (resourceIds.isEmpty()) {
            return;
        }

        resourceIds = outboxRepository.lock(resourceIds);
        if (resourceIds.isEmpty()) {
            return;
        }

        log.debug("Immediate processing DELETE for {} resource IDs", resourceIds.size());

        try {
            songServiceClient.deleteSongMetadataCSV(resourceIds);

            int removed = deleteOutboxEvents(
                    OutboxEventType.DELETE_METADATA,
                    resourceIds
            );

            log.info("Immediate DELETE delivered for {} resources, removed {} outbox events",
                    resourceIds.size(), removed);
        } catch (Exception ex) {
            String detailedError = extractDetailedError(ex);
            markAsFailed(OutboxEventType.DELETE_METADATA, resourceIds, detailedError);

            log.warn("Immediate DELETE failed for {} resources. Error: {}", resourceIds.size(), detailedError);
        }
    }

    @Transactional
    public void processCreateBatchOnce() {
        List<OutboxEvent> events = outboxRepository.findBatchForProcessing(
                OutboxEventType.CREATE_METADATA.name(),
                outboxConfig.getMaxAttempts(),
                outboxConfig.getCreateBatchSize()
        );

        if (events.isEmpty()) {
            return;
        }

        log.info("Processing CREATE batch: {} events", events.size());

        Set<Long> resourceIds = events.stream()
                .map(OutboxEvent::getEntityId)
                .collect(Collectors.toSet());

        try {
            String bulkJson = buildBulkCreateJson(events);
            songServiceClient.createSongMetadataBulkJson(bulkJson);

            int removed = deleteOutboxEvents(OutboxEventType.CREATE_METADATA, resourceIds);

            log.info("Batch CREATE delivered for {} resources, removed {} outbox events",
                    resourceIds.size(), removed);
        } catch (Exception ex) {
            String detailedError = extractDetailedError(ex);
            int updated = markAsFailed(OutboxEventType.CREATE_METADATA, resourceIds, detailedError);

            log.warn("Batch CREATE failed for {} resources, marked {} as failed. Error: {}",
                    resourceIds.size(), updated, detailedError);
        }
    }

    @Transactional
    public void processDeleteBatchOnce() {
        List<OutboxEvent> events = outboxRepository.findBatchForProcessing(
                OutboxEventType.DELETE_METADATA.name(),
                outboxConfig.getMaxAttempts(),
                outboxConfig.getDeleteBatchSize()
        );

        if (events.isEmpty()) {
            return;
        }

        log.info("Processing DELETE batch: {} events", events.size());

        Set<Long> resourceIds = events.stream()
                .map(OutboxEvent::getEntityId)
                .collect(Collectors.toSet());

        try {
            songServiceClient.deleteSongMetadataBulk(resourceIds);

            int removed = deleteOutboxEvents(OutboxEventType.DELETE_METADATA, resourceIds);

            log.info("Batch DELETE delivered for {} resources, removed {} outbox events",
                    resourceIds.size(), removed);
        } catch (Exception ex) {
            String detailedError = extractDetailedError(ex);
            int updated = markAsFailed(OutboxEventType.DELETE_METADATA, resourceIds, detailedError);

            log.warn("Batch DELETE failed for {} resources, marked {} as failed. Error: {}",
                    resourceIds.size(), updated, detailedError);
        }
    }

    private int deleteOutboxEvents(OutboxEventType type, Set<Long> resourceIds) {
        return outboxRepository.deleteByTypeAndEntityIds(type, resourceIds);
    }

    private int markAsFailed(OutboxEventType type, Set<Long> resourceIds, String error) {
        return outboxRepository.markFailedByTypeAndEntityIds(type, resourceIds, error);
    }

    private String buildBulkCreateJson(List<OutboxEvent> events) {
        try {
            ArrayNode songs = objectMapper.createArrayNode();

            for (OutboxEvent event : events) {
                JsonNode payloadNode = objectMapper.readTree(event.getPayload());
                if (!payloadNode.isObject()) {
                    throw new IllegalArgumentException(
                            "Invalid outbox payload for event ID: " + event.getId()
                    );
                }
                songs.add(payloadNode);
            }

            ObjectNode root = objectMapper.createObjectNode();
            root.set("songs", songs);
            return root.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to build bulk create JSON", e);
        }
    }

    private String extractDetailedError(Throwable ex) {
        HttpStatusCodeException httpEx = findCause(ex, HttpStatusCodeException.class).orElse(null);
        if (httpEx != null) {
            String responseBody = httpEx.getResponseBodyAsString();
            if (!responseBody.isBlank()) {
                return truncate("HTTP " + httpEx.getStatusCode().value() + " - " + responseBody, 2000);
            }
            return truncate("HTTP " + httpEx.getStatusCode().value() + " - " + httpEx.getStatusText(), 2000);
        }

        if (ex instanceof ResourceAccessException) {
            return truncate("Connection error: " + ex.getMessage(), 2000);
        }

        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return truncate(message, 2000);
    }

    private boolean isConflict409(Throwable ex) {
        return findCause(ex, HttpStatusCodeException.class)
                .map(httpException -> httpException.getStatusCode().value() == 409)
                .orElse(false);
    }

    private static <T extends Throwable> Optional<T> findCause(Throwable ex, Class<T> type) {
        Throwable current = ex;
        while (current != null) {
            if (type.isInstance(current)) {
                return Optional.of(type.cast(current));
            }
            current = current.getCause();
        }
        return Optional.empty();
    }

    private static String truncate(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "Unknown error";
        }
        return text.length() > maxLength
                ? text.substring(0, maxLength) + "... (truncated)"
                : text;
    }
}