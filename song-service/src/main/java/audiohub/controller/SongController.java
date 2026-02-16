package audiohub.controller;

import audiohub.dto.CreateSongResponse;
import audiohub.dto.DeleteSongsResponse;
import audiohub.dto.SongDto;
import audiohub.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<CreateSongResponse> createSong(@Valid @RequestBody SongDto songDto) {
        CreateSongResponse response = songService.createSong(songDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable String id) {
        SongDto song = songService.getSong(id);

        return ResponseEntity.ok(song);
    }

    @DeleteMapping
    public ResponseEntity<DeleteSongsResponse> deleteSongs(@RequestParam("id") String idCsv) {
        DeleteSongsResponse response = songService.deleteSongs(idCsv);

        return ResponseEntity.ok(response);
    }
}