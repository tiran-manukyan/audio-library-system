package audiohub.service;

import audiohub.dto.request.SongMetadataDto;
import audiohub.repository.OutboxBulkRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxBulkRepository outboxBulkRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueueCreateMetadata(long resourceId, SongMetadataDto metadata) {
        String payload = serializeMetadataWithId(resourceId, metadata);
        outboxBulkRepository.upsertCreateMetadataEvent(resourceId, payload);
        log.debug("Enqueued CREATE_METADATA event for resource ID: {}", resourceId);
    }

    @Transactional
    public void enqueueDeleteMetadata(Set<Long> resourceIds) {
        if (resourceIds.isEmpty()) {
            return;
        }

        outboxBulkRepository.insertDeleteMetadataEvents(resourceIds);
        log.debug("Enqueued {} DELETE_METADATA events", resourceIds.size());
    }

    private String serializeMetadataWithId(long resourceId, SongMetadataDto metadata) {
        try {
            ObjectNode payload = objectMapper.valueToTree(metadata);
            payload.put("id", resourceId);
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize metadata for resource ID: " + resourceId,
                    e
            );
        }
    }
}