package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrievalPayloadRedisServiceTest {

    private ReactiveRedisTemplate<String, RetrievalPayload> retrievalPayloadOps;
    private ReactiveValueOperations<String, RetrievalPayload> valueOperations;
    private RetrievalPayloadRedisService retrievalPayloadRedisService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void init() {
        retrievalPayloadOps = mock(ReactiveRedisTemplate.class);
        valueOperations = mock(ReactiveValueOperations.class);
        when(retrievalPayloadOps.opsForValue()).thenReturn(valueOperations);
        retrievalPayloadRedisService = new RetrievalPayloadRedisService(retrievalPayloadOps);
    }

    @Test
    void getRetrievalPayloadFromCacheSuccessfully() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        when(valueOperations.get(any(String.class))).thenReturn(Mono.just(expectedPayload));

        StepVerifier.create(retrievalPayloadRedisService.get(retrievalId))
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void getRetrievalPayloadFromCacheNotFound() {
        when(valueOperations.get(any(String.class))).thenReturn(Mono.empty());

        StepVerifier.create(retrievalPayloadRedisService.get("retrievalId"))
                .verifyComplete();
    }

    @Test
    void getRetrievalPayloadFromCacheFails() {
        when(valueOperations.get(any(String.class))).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(retrievalPayloadRedisService.get("retrievalId"))
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheSuccessfully() {
        when(valueOperations.set(any(String.class), any(RetrievalPayload.class)))
                .thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(retrievalPayloadRedisService.set("retrievalId", new RetrievalPayload()))
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheFails() {
        when(valueOperations.set(any(String.class), any(RetrievalPayload.class)))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(retrievalPayloadRedisService.set("retrievalId", new RetrievalPayload()))
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheWithTTLSuccessfully() {
        when(valueOperations.set(any(String.class), any(RetrievalPayload.class), any(Duration.class)))
                .thenReturn(Mono.just(Boolean.TRUE));

        StepVerifier.create(retrievalPayloadRedisService.set("retrievalId", new RetrievalPayload(), Duration.ofMinutes(5)))
                .verifyComplete();
    }

    @Test
    void setRetrievalPayloadInCacheWithTTLFails() {
        when(valueOperations.set(any(String.class), any(RetrievalPayload.class), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(retrievalPayloadRedisService.set("retrievalId", new RetrievalPayload(), Duration.ofMinutes(5)))
                .verifyComplete();
    }

    @Test
    void deleteRetrievalPayloadFromCacheSuccessfully() {
        when(valueOperations.getAndDelete(any(String.class))).thenReturn(Mono.just(new RetrievalPayload()));

        StepVerifier.create(retrievalPayloadRedisService.delete("retrievalId"))
                .verifyComplete();
    }

    @Test
    void deleteRetrievalPayloadFromCacheFails() {
        when(valueOperations.getAndDelete(any(String.class))).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(retrievalPayloadRedisService.delete("retrievalId"))
                .verifyComplete();
    }
}
