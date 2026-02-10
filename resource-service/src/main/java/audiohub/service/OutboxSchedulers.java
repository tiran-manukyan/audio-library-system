package audiohub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "outbox.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxSchedulers {

    private final OutboxPublisherFacade outboxPublisherFacade;

    @Scheduled(fixedDelayString = "#{@outboxConfig.scheduler.createDelay.toMillis()}")
    public void publishCreates() {
        try {
            log.debug("Starting CREATE batch processing");
            outboxPublisherFacade.processCreateBatchOnce();
        } catch (Exception e) {
            log.error("Error in CREATE batch processing", e);
        }
    }

    @Scheduled(fixedDelayString = "#{@outboxConfig.scheduler.deleteDelay.toMillis()}")
    public void publishDeletes() {
        try {
            log.debug("Starting DELETE batch processing");
            outboxPublisherFacade.processDeleteBatchOnce();
        } catch (Exception e) {
            log.error("Error in DELETE batch processing", e);
        }
    }
}