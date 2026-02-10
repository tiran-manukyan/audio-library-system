package audiohub.controller;

import audiohub.dto.response.DeleteResourcesResponse;
import audiohub.dto.response.UploadResourceResponse;
import audiohub.service.Mp3ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceFileController {

    private final Mp3ResourceService resourceService;

    @PostMapping(consumes = "audio/mpeg")
    public ResponseEntity<UploadResourceResponse> upload(@RequestBody byte[] audioData) {
        UploadResourceResponse response = resourceService.store(audioData);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        byte[] audioData = resourceService.getResource(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(audioData);
    }

    @DeleteMapping
    public ResponseEntity<DeleteResourcesResponse> delete(@RequestParam("id") String idCsv) {
        DeleteResourcesResponse deleteResourcesResponse = resourceService.deleteResources(idCsv);

        return ResponseEntity.ok(deleteResourcesResponse);
    }
}