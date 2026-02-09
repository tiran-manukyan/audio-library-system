package audiohub.controller;

import audiohub.dto.DeleteSongsResponse;
import audiohub.dto.SongDto;
import audiohub.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/songs")
public class SongController {

    private final SongService service;

    @PostMapping
    public ResponseEntity<SongDto> createSong(@RequestBody SongDto songDto) {
        SongDto created = service.createSong(songDto);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping
    public ResponseEntity<DeleteSongsResponse> deleteSongs(@RequestParam("id") String idParam) {
        List<Long> ids = Arrays.stream(idParam.split(",")).map(String::trim).map(Long::parseLong).toList();
        DeleteSongsResponse deleted = service.deleteSongs(ids);
        return ResponseEntity.ok(deleted);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable Long id) {
        SongDto song = service.getSong(id);
        return ResponseEntity.ok(song);
    }
}