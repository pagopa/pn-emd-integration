package it.pagopa.pn.emd.integration.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Pre-establishes the Lettuce connection to Redis during Spring context initialization,
 * before the Netty HTTP server starts accepting requests.
 *
 * <p>Without this warm-up, the first call to {@code getReactiveConnection()} triggers
 * lazy initialization: event loop allocation + DNS + TCP + TLS + HELLO takes ~4 seconds.
 * Since the ALB health check timeout is 5 seconds, this first call consistently runs
 * dangerously close to the timeout, causing intermittent AbortedException crashes.
 *
 * <p>Running in {@code @PostConstruct} ensures the 4-second cold start is absorbed
 * during container startup (before any health check can arrive), so all subsequent
 * health checks hit a warm connection (~5 ms ping latency).
 */
@Slf4j
@Component
@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
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
                    .block(Duration.ofSeconds(15));

            if (response != null) {
                log.info("[REDIS-WARMUP] Redis connection ready in {} ms (response: {})",
                        System.currentTimeMillis() - start, response);
            }
        } catch (Exception ex) {
            log.warn("[REDIS-WARMUP] Warm-up exception (non-fatal): {}", ex.getMessage());
        }
    }
}
