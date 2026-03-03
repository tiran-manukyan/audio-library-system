package audiohub.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;


@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "outbox")
public class OutboxConfig {

    @NotNull
    @Min(1)
    private Integer maxAttempts = 5;

    @NotNull
    @Min(1)
    private Integer createBatchSize = 50;

    @NotNull
    @Min(1)
    private Integer deleteBatchSize = 200;

    @NotNull
    private Scheduler scheduler = new Scheduler();

    @Getter
    @Setter
    public static class Scheduler {

        private Boolean enabled = true;

        @NotNull
        private Duration createDelay = Duration.ofSeconds(5);

        @NotNull
        private Duration deleteDelay = Duration.ofSeconds(60);
    }
}