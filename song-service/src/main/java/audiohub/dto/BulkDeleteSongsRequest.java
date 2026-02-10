package audiohub.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record BulkDeleteSongsRequest(
        @NotEmpty(message = "At least one id must be provided")
        Set<@NotNull Long> ids
) {
}