package audiohub.service;

import audiohub.dto.request.SongMetadataDto;
import audiohub.exception.SongServiceException;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongServiceClient {

    private final RestClient songServiceRestClient;

    public void createSongMetadata(Long resourceId, SongMetadataDto metadata) {
        try {
            log.debug("Creating metadata for resource ID: {}", resourceId);

            CreateMetadataRequest request = new CreateMetadataRequest(resourceId, metadata);

            songServiceRestClient.post()
                    .uri("/songs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new SongServiceException(
                    "Failed to create metadata for resource ID: " + resourceId,
                    e
            );
        }
    }

    public void createSongMetadataBulkJson(String bulkBodyJson) {
        try {
            log.debug("Bulk creating metadata");

            songServiceRestClient.post()
                    .uri("/songs/bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(bulkBodyJson)
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new SongServiceException(
                    "Failed to bulk create metadata",
                    e
            );
        }
    }

    public void deleteSongMetadataCSV(Set<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }

        try {
            log.info("Deleting song metadata for resource IDs: {}", ids);

            String idsParam = ids.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            songServiceRestClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/songs")
                            .queryParam("id", idsParam)
                            .build())
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new SongServiceException(
                    "Failed to delete song metadata for resource IDs: " + ids,
                    e
            );
        }
    }

    public void deleteSongMetadataBulk(Set<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }

        try {
            log.debug("Bulk deleting metadata for {} resource IDs", ids.size());

            songServiceRestClient.post()
                    .uri("/songs/delete-bulk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new BulkDeleteRequest(ids))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {
            throw new SongServiceException(
                    "Failed to bulk delete metadata for IDs: " + ids,
                    e
            );
        }
    }

    private record BulkDeleteRequest(Set<Long> ids) {
    }

    private record CreateMetadataRequest(
            long id,
            @JsonUnwrapped SongMetadataDto metadata
    ) {
    }
}