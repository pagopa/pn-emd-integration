package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImplV1;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmdRetrievalServiceImplTest {

    @Mock
    private EmdClientImpl emdClient;

    @Mock
    private AccessTokenExpiringMap accessTokenExpiringMap;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Mock
    private ReactiveRedisService<RetrievalPayload> redisService;

    @InjectMocks
    private EmdRetrievalServiceImpl emdRetrievalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getTokenRetrievalPayloadReturnsPayload() {
        String retrievalId = "retrievalId";
        Boolean isPaymentEnabled = true;

        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);
        expectedPayload.setIsPaymentEnabled(isPaymentEnabled);
        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);
        responseDTO.setIsPaymentEnabled(isPaymentEnabled);

        mockAccessTokenExpiringMap();
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));
        when(pnEmdIntegrationConfigs.getRetrievalPayloadCacheTtl()).thenReturn(Duration.ofMinutes(5));
        when(redisService.set(any(String.class), any(RetrievalPayload.class), any(Duration.class))).thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId) && payload.getIsPaymentEnabled().equals(true))
                .verifyComplete();
    }

    @Test
    void getTokenRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void getTokenRetrievalPayloadHandlesError() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Generic Error"))
                .verify();
    }

    @Test
    void getEmdRetrievalPayloadReturnsPayloadFromCache() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        mockAccessTokenExpiringMap();
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(redisService.get(retrievalId)).thenReturn(Mono.just(expectedPayload));

        Mono<RetrievalPayload> result = emdRetrievalService.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void getEmdRetrievalPayloadReturnsPayloadFromClient() {
        String retrievalId = "retrievalId";
        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        mockAccessTokenExpiringMap();
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(redisService.get(retrievalId)).thenReturn(Mono.empty());
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));

        Mono<RetrievalPayload> result = emdRetrievalService.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void getEmdRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(redisService.get(retrievalId)).thenReturn(Mono.empty());
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.empty());

        mockAccessTokenExpiringMap();
        Mono<RetrievalPayload> result = emdRetrievalService.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void getEmdRetrievalPayloadHandlesError() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(redisService.get(retrievalId)).thenReturn(Mono.empty());
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        Mono<RetrievalPayload> result = emdRetrievalService.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Generic Error"))
                .verify();
    }

    private void mockAccessTokenExpiringMap() {
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");
        when(pnEmdIntegrationConfigs.getEnableApiV2()).thenReturn(true);
        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
    }
}
