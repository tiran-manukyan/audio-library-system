package audiohub.controller;

import audiohub.dto.response.DeleteResourcesResponse;
import audiohub.dto.response.ResourceFileResponse;
import audiohub.entity.ResourceFileEntity;
import audiohub.service.ResourceFileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceFileController {

    private final ResourceFileService resourceFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceFileResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceFileService.store(file));
    }

    @GetMapping("/{id}")
    public void download(@PathVariable String id, HttpServletResponse response) throws IOException {
        ResourceFileEntity resource = resourceFileService.findById(id);

        response.setContentType(resource.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + StringUtils.cleanPath(resource.getFilename()) + "\"");
        response.getOutputStream().write(resource.getData());
        response.flushBuffer();
    }

    @DeleteMapping
    public ResponseEntity<DeleteResourcesResponse> delete(@RequestParam("id") String idParam) {
        DeleteResourcesResponse deleteResourcesResponse = resourceFileService.deleteResources(idParam);

        return ResponseEntity.ok(deleteResourcesResponse);
    }
}