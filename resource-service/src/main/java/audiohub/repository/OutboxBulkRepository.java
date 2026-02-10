package audiohub.repository;

import java.util.Set;

public interface OutboxBulkRepository {

    void upsertCreateMetadataEvent(long entityIds, String payload);

    void insertDeleteMetadataEvents(Set<Long> entityIds);
}