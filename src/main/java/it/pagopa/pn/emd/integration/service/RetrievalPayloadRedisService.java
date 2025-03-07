package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetrievalPayloadRedisService implements ReactiveRedisService<RetrievalPayload> {
    private final RedisTemplate<String, RetrievalPayload> retrievalPayloadOps;

    private static final String CACHE_PREFIX = NAMESPACE + "retrievalPayload::";

    @Override
    public Mono<RetrievalPayload> get(String retrievalId) {
        return Mono.fromCallable(() -> retrievalPayloadOps.opsForValue().get(composeCacheKey(retrievalId)))
                .onErrorResume(throwable -> {
                    log.warn("Error getting retrievalId: {} from cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .doOnNext(result -> log.info("Get retrievalId: {} in cache successfully: {}", retrievalId, result))
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("RetrievalId: {} not found in cache", retrievalId);
                    return Mono.empty();
                }));
    }

    @Override
    public Mono<Void> set(String retrievalId, RetrievalPayload payload) {
        return Mono.fromRunnable(() -> retrievalPayloadOps.opsForValue().set(composeCacheKey(retrievalId), payload))
                .doOnSuccess(unused -> log.info("Set retrievalId: {} in cache successfully", retrievalId))
                .onErrorResume(throwable -> {
                    log.warn("Error setting retrievalId: {} in cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Void> set(String retrievalId, RetrievalPayload payload, Duration ttl) {
        return Mono.fromRunnable(() -> retrievalPayloadOps.opsForValue().set(composeCacheKey(retrievalId), payload, ttl))
                .doOnSuccess(unused -> log.info("Set retrievalId: {} in cache successfully with ttl of : {} seconds", retrievalId, ttl.getSeconds()))
                .onErrorResume(throwable -> {
                    log.warn("Error setting retrievalId: {} in cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Void> delete(String retrievalId) {
        return Mono.fromRunnable(() -> retrievalPayloadOps.opsForValue().getAndDelete(composeCacheKey(retrievalId)))
                .doOnNext(unused -> log.info("Delete retrievalId: {} from cache successfully", retrievalId))
                .onErrorResume(throwable -> {
                    log.warn("Error deleting retrievalId: {} in cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .then();
    }

    private String composeCacheKey(String retrievalId) {
        return CACHE_PREFIX + retrievalId;
    }
}
