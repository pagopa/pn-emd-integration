package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdpayment.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmdRetrievalServiceImplTest {

    @Mock
    private EmdClientImpl emdClient;

    @Mock
    private AccessTokenExpiringMap accessTokenExpiringMap;

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

        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);
        responseDTO.setIsPaymentEnabled(isPaymentEnabled);

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                    .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId) && payload.getIsPaymentEnabled().equals(true))
                    .verifyComplete();
    }

    @Test
    void getTokenRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
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
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                    .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                                                     throwable.getMessage().equals("Generic Error"))
                    .verify();
    }

    @Test
    void getEmdRetrievalPayloadReturnsPayloadFromClient() {
        String retrievalId = "retrievalId";
        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));

        Mono<RetrievalPayload> result = emdRetrievalService.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                    .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                    .verifyComplete();
    }

    @Test
    void getEmdRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdRetrievalService.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                    .expectError(PnEmdIntegrationNotFoundException.class)
                    .verify();
    }

    @Test
    void getEmdRetrievalPayloadHandlesError() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
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
        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
    }

    @Test
    void getTokenRetrievalPayloadReturnsPayload_pspDenominationNotNull() {
        String retrievalId = "retrievalId";
        Boolean isPaymentEnabled = true;
        String pspDenomination = "Banca1";

        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);
        responseDTO.setIsPaymentEnabled(isPaymentEnabled);
        responseDTO.setPspDenomination(pspDenomination);

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                    .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId) && payload.getIsPaymentEnabled().equals(true) && payload.getPspDenomination().equals(pspDenomination))
                    .verifyComplete();
    }

    @Test
    void getTokenRetrievalPayloadReturnsPayload_isPaymentEnabledNull() {
        String retrievalId = "retrievalId";
        String pspDenomination = "Banca1";

        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);
        responseDTO.setPspDenomination(pspDenomination);

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));

        Mono<RetrievalPayload> result = emdRetrievalService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                    .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId) && payload.getIsPaymentEnabled().equals(false))
                    .verifyComplete();
    }

}
