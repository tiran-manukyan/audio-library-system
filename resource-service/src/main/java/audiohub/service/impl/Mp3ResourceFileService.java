package audiohub.service.impl;

import audiohub.dto.request.MetadataRequest;
import audiohub.dto.response.DeleteResourcesResponse;
import audiohub.dto.response.ResourceFileResponse;
import audiohub.entity.ResourceFileEntity;
import audiohub.exception.InvalidFileTypeException;
import audiohub.repository.ResourceFileRepository;
import audiohub.service.MetadataExtractor;
import audiohub.service.ResourceFileService;
import audiohub.util.ResourceIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class Mp3ResourceFileService implements ResourceFileService {

    private final ResourceFileRepository repository;
    private final MetadataExtractor metadataExtractor;
    private final SongServiceClient metadataClient;

    private static final Set<String> SUPPORTED_MP3_TYPES = Set.of(
            "audio/mpeg", "audio/mp3", "audio/x-mpeg",
            "audio/x-mp3", "audio/x-mpeg-3", "audio/mpeg3"
    );

    @Override
    @Transactional
    public ResourceFileResponse store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_MP3_TYPES.contains(contentType)) {
            throw new InvalidFileTypeException("Only MP3 files (audio/mpeg) are allowed.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw new InvalidFileTypeException("File extension must be .mp3");
        }

        try {
            var resource = ResourceFileEntity.builder()
                    .data(file.getBytes())
                    .filename(filename)
                    .contentType(contentType)
                    .uploadTime(LocalDateTime.now())
                    .build();

            resource = repository.save(resource);

            Map<String, Object> meta = metadataExtractor.extract(file);

            metadataClient.sendMetadata(
                    MetadataRequest.builder()
                            .resourceId(resource.getId())
                            .name((String) meta.get("title"))
                            .artist((String) meta.get("artist"))
                            .album((String) meta.get("album"))
                            .year((Integer) meta.get("year"))
                            .duration((String) meta.get("duration"))
                            .build()
            );

            return new ResourceFileResponse(resource.getId(), filename, contentType);
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes: {}", filename, e);
            throw new RuntimeException("Failed to read file bytes", e);
        } catch (Exception e) {
            log.error("General error in file upload: {}", filename, e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public ResourceFileEntity findById(String id) {
        Long parsedId = ResourceIdUtil.parsePositiveId(id);

        return repository.findById(parsedId)
                .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
    }

    @Override
    @Transactional
    public DeleteResourcesResponse deleteResources(String idCsv) {
        List<Long> ids = ResourceIdUtil.parsePositiveIds(idCsv);

        List<Long> existingIds = repository.findExistingIds(ids);
        if (!existingIds.isEmpty()) {
            repository.deleteAllByIdInBatch(existingIds);
            metadataClient.deleteMetadata(existingIds);
        }

        return new DeleteResourcesResponse(existingIds);
    }
}