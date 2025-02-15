package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrievalPayloadRedisServiceTest {
    private ReactiveRedisTemplate<String, RetrievalPayload> retrievalPayloadOps;

    private RetrievalPayloadRedisService retrievalPayloadRedisService;

    @BeforeEach
    public void init() {
        retrievalPayloadOps = mock(ReactiveRedisTemplate.class);
        when(retrievalPayloadOps.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ReactiveValueOperations.class));
        retrievalPayloadRedisService = new RetrievalPayloadRedisService(retrievalPayloadOps);
    }

    @Test
    void getRetrievalPayloadFromCacheSuccessfully() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        when(retrievalPayloadOps.opsForValue().get(any(String.class))).thenReturn(Mono.just(expectedPayload));

        Mono<RetrievalPayload> result = retrievalPayloadRedisService.get(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void getRetrievalPayloadFromCacheNotFound() {
        String retrievalId = "retrievalId";

        when(retrievalPayloadOps.opsForValue().get(any(String.class))).thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = retrievalPayloadRedisService.get(retrievalId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getRetrievalPayloadFromCacheFails() {
        String retrievalId = "retrievalId";

        when(retrievalPayloadOps.opsForValue().get(any(String.class))).thenReturn(Mono.error(new RuntimeException()));

        Mono<RetrievalPayload> result = retrievalPayloadRedisService.get(retrievalId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheSuccessfully() {
        String retrievalId = "retrievalId";
        RetrievalPayload payload = new RetrievalPayload();
        payload.setRetrievalId(retrievalId);

        when(retrievalPayloadOps.opsForValue().set(any(String.class), any(RetrievalPayload.class))).thenReturn(Mono.just(true));

        Mono<Void> result = retrievalPayloadRedisService.set(retrievalId, payload);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheFails() {
        String retrievalId = "retrievalId";
        RetrievalPayload payload = new RetrievalPayload();
        payload.setRetrievalId(retrievalId);

        when(retrievalPayloadOps.opsForValue().set(any(String.class), any(RetrievalPayload.class))).thenReturn(Mono.error(new RuntimeException()));

        Mono<Void> result = retrievalPayloadRedisService.set(retrievalId, payload);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheWithTTLSuccessfully() {
        String retrievalId = "retrievalId";
        RetrievalPayload payload = new RetrievalPayload();
        payload.setRetrievalId(retrievalId);
        Duration ttl = Duration.ofMinutes(5);

        when(retrievalPayloadOps.opsForValue().set(any(String.class), any(RetrievalPayload.class), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<Void> result = retrievalPayloadRedisService.set(retrievalId, payload, ttl);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheWithTTLFails() {
        String retrievalId = "retrievalId";
        RetrievalPayload payload = new RetrievalPayload();
        payload.setRetrievalId(retrievalId);
        Duration ttl = Duration.ofMinutes(5);

        when(retrievalPayloadOps.opsForValue().set(any(String.class), any(RetrievalPayload.class), any(Duration.class))).thenReturn(Mono.error(new RuntimeException()));

        Mono<Void> result = retrievalPayloadRedisService.set(retrievalId, payload, ttl);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteRetrievalPayloadFromCacheSuccessfully() {
        String retrievalId = "retrievalId";

        when(retrievalPayloadOps.opsForValue().delete(any(String.class))).thenReturn(Mono.just(true));

        Mono<Void> result = retrievalPayloadRedisService.delete(retrievalId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void deleteRetrievalPayloadFromCacheFails() {
        String retrievalId = "retrievalId";

        when(retrievalPayloadOps.opsForValue().delete(any(String.class))).thenReturn(Mono.error(new RuntimeException()));

        Mono<Void> result = retrievalPayloadRedisService.delete(retrievalId);

        StepVerifier.create(result)
                .verifyComplete();
    }
}