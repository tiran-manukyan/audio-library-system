package audiohub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SongDto(

        @NotNull(message = "ID is required")
        Long id,

        @NotBlank(message = "Song name is required")
        @Size(max = 100, message = "Name must be between 1 and 100 characters")
        String name,

        @NotBlank(message = "Artist name is required")
        @Size(max = 100, message = "Artist name must be between 1 and 100 characters")
        String artist,

        @NotBlank(message = "Album name is required")
        @Size(max = 100, message = "Album must be between 1 and 100 characters")
        String album,

        @NotBlank(message = "Year is required")
        @Pattern(regexp = "^(19|20)\\d{2}$", message = "Year must be between 1900 and 2099")
        String year,

        @NotBlank(message = "Duration is required")
        @Pattern(regexp = "^\\d{2}:[0-5]\\d$", message = "Duration must be in mm:ss format with leading zeros")
        String duration
) {
}