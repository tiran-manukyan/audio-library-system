package audiohub.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("song-service", r -> r
                        .path("/song-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://song-service"))

                .route("resource-service", r -> r
                        .path("/resource-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://resource-service"))

                .build();
    }
}