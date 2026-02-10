package audiohub.service;

import audiohub.dto.request.SongMetadataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalEventPublisher {

    private final OutboxPublisherFacade outboxPublisherFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResourceCreated(ResourceCreatedEvent event) {
        log.debug("Transaction committed, triggering immediate CREATE processing for resource: {}", 
                  event.resourceId());
        outboxPublisherFacade.processCreateEvent(event.resourceId(), event.metadata());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResourcesDeleted(ResourcesDeletedEvent event) {
        log.debug("Transaction committed, triggering immediate DELETE processing for {} resources", 
                  event.deletedIds().size());
        outboxPublisherFacade.processDeleteEvents(event.deletedIds());
    }

    public record ResourceCreatedEvent(Long resourceId, SongMetadataDto metadata) {}
    public record ResourcesDeletedEvent(Set<Long> deletedIds) {}
}