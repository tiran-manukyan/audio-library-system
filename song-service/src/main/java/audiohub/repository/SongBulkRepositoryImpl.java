package audiohub.repository;

import audiohub.dto.SongDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SongBulkRepositoryImpl implements SongBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insertIgnoreConflictsById(Collection<SongDto> songs) {
        if (songs == null || songs.isEmpty()) {
            return;
        }

        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>(songs.size() * 6);

        int i = 0;
        for (SongDto s : songs) {
            if (i++ > 0) values.append(", ");
            values.append("(?, ?, ?, ?, ?, ?)");

            params.add(s.id());
            params.add(s.name());
            params.add(s.artist());
            params.add(s.album());
            params.add(s.year());
            params.add(s.duration());
        }

        String sql = """
                INSERT INTO songs (id, name, artist, album, year, duration)
                VALUES %s
                ON CONFLICT (id) DO NOTHING
                """.formatted(values);

        jdbcTemplate.update(sql, params.toArray());
    }
}