package audiohub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

        log.info("Incoming request: {} {} | Route: {} -> {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                route != null ? route.getId() : "N/A",
                route != null ? route.getUri() : "N/A"
        );

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;

                    log.info("Completed: {} {} | Status: {} | Time: {} ms",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI(),
                            exchange.getResponse().getStatusCode(),
                            duration
                    );
                })
        );
    }

    @Override
    public int getOrder() {
        return -1;
    }
}