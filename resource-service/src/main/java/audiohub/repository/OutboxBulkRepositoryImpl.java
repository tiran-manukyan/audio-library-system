package audiohub.repository;

import audiohub.entity.OutboxEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OutboxBulkRepositoryImpl implements OutboxBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void upsertCreateMetadataEvent(long entityIds, String payload) {
        String sql = """
                INSERT INTO outbox_events (type, entity_id, payload, attempts, created_at)
                VALUES (?, ?, CAST(? AS TEXT), 0, NOW())
                ON CONFLICT (type, entity_id)
                DO UPDATE SET
                    payload = EXCLUDED.payload,
                    attempts = 0,
                    last_error = NULL
                """;

        int rows = jdbcTemplate.update(
                sql,
                OutboxEventType.CREATE_METADATA.name(),
                entityIds,
                payload
        );

        log.debug("Upserted CREATE_METADATA event for entity_id={}, affected rows={}",
                entityIds, rows);
    }

    @Override
    @Transactional
    public void insertDeleteMetadataEvents(Set<Long> entityIds) {
        if (entityIds.isEmpty()) {
            return;
        }

        List<Long> idList = new ArrayList<>(entityIds);

        String sql = """
                INSERT INTO outbox_events (type, entity_id, payload, attempts, created_at)
                VALUES (?, ?, '{}', 0, NOW())
                ON CONFLICT (type, entity_id) DO NOTHING
                """;

        int[] batchResult = jdbcTemplate.batchUpdate(
                sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, OutboxEventType.DELETE_METADATA.name());
                        ps.setLong(2, idList.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return idList.size();
                    }
                }
        );

        int inserted = (int) java.util.Arrays.stream(batchResult)
                .filter(rowsAffected -> rowsAffected > 0)
                .count();

        log.debug("Inserted {} DELETE_METADATA events (out of {} requested)",
                inserted, entityIds.size());
    }
}