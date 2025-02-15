package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetrievalPayloadRedisService implements ReactiveRedisService<RetrievalPayload> {
    private final ReactiveRedisTemplate<String, RetrievalPayload> retrievalPayloadOps;

    private final static String CACHE_PREFIX = NAMESPACE + "retrievalPayload::";

    @Override
    public Mono<RetrievalPayload> get(String retrievalId) {
        return retrievalPayloadOps.opsForValue().get(composeCacheKey(retrievalId))
                .onErrorResume(throwable -> {
                    log.warn("Error getting retrievalId: {} from cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .doOnNext(result -> log.info("Get retrievalId: {} in cache successfully: {}", retrievalId, result));
    }

    @Override
    public Mono<Void> set(String retrievalId, RetrievalPayload payload) {
        return retrievalPayloadOps.opsForValue().set(composeCacheKey(retrievalId), payload)
                .doOnNext(result -> log.info("Set retrievalId: {} in cache successfully: {}", retrievalId, result))
                .onErrorResume(throwable -> {
                    log.warn("Error setting retrievalId: {} in cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Void> set(String retrievalId, RetrievalPayload payload, Duration ttl) {
        return retrievalPayloadOps.opsForValue().set(composeCacheKey(retrievalId), payload, ttl)
                .doOnNext(result -> log.info("Set retrievalId: {} in cache successfully: {} with ttl of : {} seconds", retrievalId, result, ttl.getSeconds()))
                .onErrorResume(throwable -> {
                    log.warn("Error setting retrievalId: {} in cache", retrievalId, throwable);
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Void> delete(String retrievalId) {
        return retrievalPayloadOps.opsForValue().delete(composeCacheKey(retrievalId))
                .doOnNext(result -> log.info("Delete retrievalId: {} from cache successfully: {}", retrievalId, result))
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
