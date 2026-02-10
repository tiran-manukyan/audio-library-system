package audiohub.service;

import audiohub.dto.BulkCreateSongsRequest;
import audiohub.dto.CreateSongResponse;
import audiohub.dto.DeleteSongsResponse;
import audiohub.dto.SongDto;
import audiohub.entity.SongEntity;
import audiohub.exception.InvalidSongIdException;
import audiohub.exception.SongAlreadyExistsException;
import audiohub.exception.SongNotFoundException;
import audiohub.mapper.SongMapper;
import audiohub.repository.SongBulkRepository;
import audiohub.repository.SongRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private static final String SONGS_PRIMARY_KEY_CONSTRAINT = "songs_pkey";

    @PersistenceContext
    private EntityManager entityManager;
    private final SongRepository songRepository;
    private final SongBulkRepository songBulkRepository;
    private final SongIdParser songIdParser;
    private final SongMapper mapper;

    @Transactional
    public CreateSongResponse createSong(SongDto songDto) {
        SongEntity entity = mapper.toEntity(songDto);

        try {
            entityManager.persist(entity);
            entityManager.flush();
            log.info("Created song metadata for resource ID: {}", entity.getId());
            return new CreateSongResponse(entity.getId());
        } catch (ConstraintViolationException ex) {
            handleConstraintException(songDto.id(), ex);

            throw ex;
        }
    }

    public SongDto getSong(String id) {
        Long songId;
        try {
            songId = songIdParser.parsePositiveId(id);
        } catch (InvalidSongIdException e) {
            throw new InvalidSongIdException(
                    "Invalid value '%s' for ID. Must be a positive integer".formatted(id)
            );
        }

        SongEntity songEntity = songRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException(
                        "Song metadata for ID=%s not found".formatted(id)
                ));

        return mapper.toDto(songEntity);
    }

    public void createSongsBulk(BulkCreateSongsRequest request) {
        songBulkRepository.insertIgnoreConflictsById(request.songs());
        log.info("Bulk created songs, received: {}, conflicts ignored (idempotent)",
                request.songs().size());    }

    public DeleteSongsResponse deleteSongs(String idCsv) {
        Set<Long> parsedIds = songIdParser.parsePositiveIds(idCsv);
        if (parsedIds.isEmpty()) {
            return new DeleteSongsResponse(Set.of());
        }

        Set<Long> deletedIds = songRepository.deleteAndReturnIds(parsedIds);

        log.info("Deleted {} song metadata records", deletedIds.size());

        return new DeleteSongsResponse(deletedIds);
    }

    public void deleteSongsBulk(Set<Long> ids) {
        Set<Long> actuallyDeletedIds = songRepository.deleteAndReturnIds(ids);

        log.info("Bulk deleted {} song metadata records with IDs: {}", actuallyDeletedIds.size(), actuallyDeletedIds);
    }

    private void handleConstraintException(Long resourceId, ConstraintViolationException ex) {
        ConstraintViolationException constraintEx = findCause(ex, ConstraintViolationException.class);

        if (constraintEx != null && SONGS_PRIMARY_KEY_CONSTRAINT.equalsIgnoreCase(constraintEx.getConstraintName())) {
            log.warn("Song metadata for resource ID {} already exists", resourceId);
            throw new SongAlreadyExistsException(
                    "Metadata for resource ID=%s already exists".formatted(resourceId),
                    ex
            );
        }
    }

    private static <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
        Throwable curr = ex;
        while (curr != null) {
            if (type.isInstance(curr)) return type.cast(curr);
            curr = curr.getCause();
        }
        return null;
    }
}