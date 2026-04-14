package it.pagopa.pn.emd.integration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("redisHealthIndicator")
public class RedisTimedHealthIndicator extends AbstractReactiveHealthIndicator {

    private final ReactiveRedisConnectionFactory connectionFactory;

    public RedisTimedHealthIndicator(ReactiveRedisConnectionFactory connectionFactory) {
        super("Redis health check failed");
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected Mono<Health> doHealthCheck(Health.Builder builder) {
        return Mono.fromCallable(connectionFactory::getReactiveConnection)
                .flatMap(connection -> pingWithTiming(builder, connection));
    }

    private Mono<Health> pingWithTiming(Health.Builder builder, ReactiveRedisConnection connection) {
        long[] startTime = {System.currentTimeMillis()};
        log.info("[REDIS-HEALTH] Avvio health check Redis (ping)");
        return connection.ping()
                .doOnNext(response -> log.info("[REDIS-HEALTH] Ping Redis completato in {} ms, risposta: {}",
                        System.currentTimeMillis() - startTime[0], response))
                .doOnError(ex -> log.info("[REDIS-HEALTH] Ping Redis FALLITO dopo {} ms: {}",
                        System.currentTimeMillis() - startTime[0], ex.getMessage()))
                .map(response -> builder.up().withDetail("response", response).build())
                .onErrorResume(ex -> Mono.just(builder.down(ex).build()))
                .doFinally(signal -> connection.close());
    }
}
