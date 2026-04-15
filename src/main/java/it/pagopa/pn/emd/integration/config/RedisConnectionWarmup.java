package it.pagopa.pn.emd.integration.config;

import jakarta.annotation.PostConstruct;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@CustomLog
@Component
@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
@Profile("!local")
@RequiredArgsConstructor
public class RedisConnectionWarmup {

    private final ReactiveRedisConnectionFactory connectionFactory;

    @PostConstruct
    public void warmUp() {
        log.info("[REDIS-WARMUP] Pre-establishing Redis connection (DNS + TLS + HELLO)...");
        long start = System.currentTimeMillis();
        try {
            String response = connectionFactory.getReactiveConnection()
                    .ping()
                    .onErrorResume(ex -> {
                        log.warn("[REDIS-WARMUP] Warm-up failed (non-fatal, will retry on first request): {}", ex.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response != null) {
                log.info("[REDIS-WARMUP] Redis connection ready in {} ms (response: {})",
                        System.currentTimeMillis() - start, response);
            }
        } catch (Exception ex) {
            log.warn("[REDIS-WARMUP] Warm-up exception (non-fatal): {}", ex.getMessage());
        }
    }
}
