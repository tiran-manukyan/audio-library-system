package audiohub.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkCreateSongsRequest(
        @NotEmpty(message = "At least one song must be provided")
        List<@Valid SongDto> songs
) {
}