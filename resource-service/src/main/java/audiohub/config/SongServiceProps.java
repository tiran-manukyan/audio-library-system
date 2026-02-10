package audiohub.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "song-service")
public record SongServiceProps(

        @NotBlank(message = "Song service URL must not be blank")
        String url,

        @NotNull(message = "Connect timeout must not be null")
        Duration connectTimeout,

        @NotNull(message = "Read timeout must not be null")
        Duration readTimeout
) {
}