package audiohub.repository;

import audiohub.entity.OutboxEvent;
import audiohub.entity.OutboxEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
            SELECT e.entity_id FROM outbox_events e
            WHERE entity_id IN :entityIds
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Set<Long> lock(@Param("entityIds") Set<Long> entityIds);

    @Query(value = """
            SELECT * FROM outbox_events
            WHERE type = CAST(:type AS VARCHAR)
              AND attempts < :maxAttempts
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> findBatchForProcessing(
            @Param("type") String type,
            @Param("maxAttempts") int maxAttempts,
            @Param("limit") int limit
    );

    @Modifying
    @Transactional
    @Query("""
            DELETE FROM OutboxEvent e
            WHERE e.type = :type
              AND e.entityId IN :entityIds
            """)
    int deleteByTypeAndEntityIds(
            @Param("type") OutboxEventType type,
            @Param("entityIds") Set<Long> entityIds
    );

    @Modifying
    @Transactional
    @Query("""
            UPDATE OutboxEvent e
            SET e.attempts = e.attempts + 1,
                e.lastError = :lastError
            WHERE e.type = :type
              AND e.entityId IN :entityIds
            """)
    int markFailedByTypeAndEntityIds(
            @Param("type") OutboxEventType type,
            @Param("entityIds") Set<Long> entityIds,
            @Param("lastError") String lastError
    );
}