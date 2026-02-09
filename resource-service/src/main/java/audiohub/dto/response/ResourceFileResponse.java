package audiohub.dto.response;

public record ResourceFileResponse(
        Long id,
        String filename,
        String contentType
) {
}