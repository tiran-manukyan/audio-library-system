package audiohub.service.impl;

import audiohub.dto.request.MetadataRequest;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceClient {

    @Value("${song.service.url}")
    private String songServiceUrl;

    private final WebClient webClient = WebClient.create();

    public void sendMetadata(MetadataRequest request) {
/*        webClient.post()
                .uri(songServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();*/
    }

    public void deleteMetadata(List<Long> ids) {
        String param = String.join(",", ids.stream().map(Object::toString).toList());
        webClient.delete()
                .uri(songServiceUrl + "?id=" + param)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}