package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
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

class EmdCoreServiceImplTest {

    @Mock
    private EmdClientImpl emdClient;

    @Mock
    private AccessTokenExpiringMap accessTokenExpiringMap;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Mock
    private ReactiveRedisService<RetrievalPayload> redisService;

    @InjectMocks
    private EmdCoreServiceImpl emdCoreServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitMessage() {
        // Arrange
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(true);
        requestBody.setOriginId("originId");
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");
        InlineResponse200 response = new InlineResponse200();
        response.setOutcome(Outcome.OK);

        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
        when(emdClient.submitMessage(any(SendMessageRequest.class),any(String.class),any(String.class))).thenReturn(Mono.just(response));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        // Act
        Mono<InlineResponse200> result = emdCoreServiceImpl.submitMessage(requestBody);

        // Assert
        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testSubmitMessageError() {
        // Arrange
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(true);
        requestBody.setOriginId("originId");
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");

        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
        when(emdClient.submitMessage(any(SendMessageRequest.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        // Act
        Mono<InlineResponse200> result = emdCoreServiceImpl.submitMessage(requestBody);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
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
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));
        when(pnEmdIntegrationConfigs.getRetrievalPayloadCacheTtl()).thenReturn(Duration.ofMinutes(5));
        when(redisService.set(any(String.class), any(RetrievalPayload.class), any(Duration.class))).thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId) && payload.getIsPaymentEnabled().equals(true) )
                .verifyComplete();
    }


    @Test
    void getTokenRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getTokenRetrievalPayload(retrievalId);

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

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getTokenRetrievalPayload(retrievalId);

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
        when(redisService.get(retrievalId)).thenReturn(Mono.just(expectedPayload));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getEmdRetrievalPayload(retrievalId);

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
        when(redisService.get(retrievalId)).thenReturn(Mono.empty());
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void getEmdRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        when(redisService.get(retrievalId)).thenReturn(Mono.empty());
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.empty());

        mockAccessTokenExpiringMap();
        Mono<RetrievalPayload> result = emdCoreServiceImpl.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void getEmdRetrievalPayloadHandlesError() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(redisService.get(retrievalId)).thenReturn(Mono.empty());
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Generic Error"))
                .verify();
    }

    @Test
    void getPaymentUrlReturnsCorrectUrl() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        Integer amount = 1000;
        String emdPaymentEndpoint = "http://example.com/emd_endpoint";

        when(pnEmdIntegrationConfigs.getEmdPaymentEndpoint()).thenReturn(emdPaymentEndpoint);

        Mono<PaymentUrlResponse> result = emdCoreServiceImpl.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getPaymentUrl().equals("http://example.com/emd_endpoint?retrievalId=retrievalId&fiscalCode=paTaxId&noticeNumber=noticeCode&amount=1000"))
                .verifyComplete();
    }

    @Test
    void getPaymentUrlReturnsCorrectUrlWithoutAmount() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        String emdPaymentEndpoint = "http://example.com/emd_endpoint";

        when(pnEmdIntegrationConfigs.getEmdPaymentEndpoint()).thenReturn(emdPaymentEndpoint);

        Mono<PaymentUrlResponse> result = emdCoreServiceImpl.getPaymentUrl(retrievalId, noticeCode, paTaxId, null);

        StepVerifier.create(result)
                    .expectNextMatches(response -> response.getPaymentUrl().equals("http://example.com/emd_endpoint?retrievalId=retrievalId&fiscalCode=paTaxId&noticeNumber=noticeCode"))
                    .verifyComplete();
    }

    private void mockAccessTokenExpiringMap() {
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");
        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
    }
}
