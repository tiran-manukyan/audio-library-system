package audiohub.service;


import audiohub.dto.DeleteSongsResponse;
import audiohub.dto.SongDto;
import audiohub.entity.SongEntity;
import audiohub.repository.SongRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository repository;

    @Transactional
    public SongDto createSong(SongDto songDto) {
        validate(songDto);

        SongEntity song = SongEntity.builder()
                .resourceId(songDto.resourceId())
                .name(songDto.name())
                .artist(songDto.artist())
                .album(songDto.album())
                .year(songDto.year())
                .duration(songDto.duration())
                .build();

        repository.save(song);
        return songDto;
    }

    @Transactional
    public DeleteSongsResponse deleteSongs(List<Long> ids) {
        List<Long> existingIds = repository.findAllById(ids)
                .stream()
                .map(SongEntity::getResourceId)
                .toList();
        if (!existingIds.isEmpty()) {
            repository.deleteAllByIdInBatch(existingIds);
        }

        return new DeleteSongsResponse(existingIds);
    }

    public SongDto getSong(Long id) {
        SongEntity song = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Song not found: " + id));
        return new SongDto(song.getResourceId(), song.getName(), song.getArtist(), song.getAlbum(), song.getYear(), song.getDuration());
    }

    private void validate(SongDto songDto) {
        if (songDto.year() < 1900 || songDto.year() > 2099) {
            throw new IllegalArgumentException("Year must be between 1900 and 2099");
        }
        if (!songDto.duration().matches("^[0-5]?\\d:[0-5]\\d$")) {
            throw new IllegalArgumentException("Duration must be in mm:ss format");
        }
    }
}