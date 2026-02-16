package audiohub.service;

import audiohub.dto.request.SongMetadataDto;
import audiohub.dto.response.DeleteResourcesResponse;
import audiohub.dto.response.UploadResourceResponse;
import audiohub.entity.ResourceEntity;
import audiohub.exception.InvalidMp3Exception;
import audiohub.exception.InvalidResourceIdException;
import audiohub.exception.InvalidSongMetadataException;
import audiohub.exception.ResourceNotFoundException;
import audiohub.repository.ResourceRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class Mp3ResourceService {

    private static final int MAX_AUDIO_SIZE_BYTES = 50 * 1024 * 1024; // 50 MiB

    private final Validator validator;
    private final ResourceRepository resourceRepository;
    private final AudioMetadataExtractor metadataExtractor;
    private final SongServiceClient songServiceClient;
    private final ResourceIdParser resourceIdParser;

    @Transactional
    public UploadResourceResponse store(byte[] audioData) {
        validateAudioSize(audioData);

        SongMetadataDto metadata = metadataExtractor.extract(audioData);

        validateSongMetadata(metadata);

        ResourceEntity resource = ResourceEntity.builder()
                .data(audioData)
                .build();

        resource = resourceRepository.save(resource);
        songServiceClient.createSongMetadata(resource.getId(), metadata);

        log.info("Saved resource with ID: {}", resource.getId());

        return new UploadResourceResponse(resource.getId());
    }

    public byte[] getResource(String id) {
        Long resourceId;
        try {
            resourceId = resourceIdParser.parsePositiveId(id);
        } catch (InvalidResourceIdException e) {
            throw new InvalidResourceIdException(
                    "Invalid value '%s' for ID. Must be a positive integer".formatted(id)
            );
        }

        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource with ID=" + id + " not found"
                )).getData();
    }

    @Transactional
    public DeleteResourcesResponse deleteResources(String idCsv) {
        Set<Long> parsedIds = resourceIdParser.parsePositiveIds(idCsv);
        if (parsedIds.isEmpty()) {
            return new DeleteResourcesResponse(Set.of());
        }

        Set<Long> deletedIds = resourceRepository.deleteAndReturnIds(parsedIds);
        if (deletedIds.isEmpty()) {
            return new DeleteResourcesResponse(Set.of());
        }

        songServiceClient.deleteSongMetadataCSV(deletedIds);

        log.info("Deleted {} resources", deletedIds.size());

        return new DeleteResourcesResponse(deletedIds);
    }

    private void validateAudioSize(byte[] audioData) {
        if (audioData != null && audioData.length > MAX_AUDIO_SIZE_BYTES) {
            throw new InvalidMp3Exception(
                    "Audio file is too large. Max allowed size is 50 MB"
            );
        }
    }

    private void validateSongMetadata(SongMetadataDto metadata) {
        Set<ConstraintViolation<SongMetadataDto>> violations = validator.validate(metadata);
        if (violations.isEmpty()) {
            return;
        }

        throw new InvalidSongMetadataException("Validation error", violations);
    }
}