package audiohub.repository;

import audiohub.dto.SongDto;

import java.util.Collection;

public interface SongBulkRepository {
    void insertIgnoreConflictsById(Collection<SongDto> songs);
}