package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
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

class EmdCoreServiceTest {

    @Mock
    private EmdClientImpl emdClient;

    @Mock
    private AccessTokenExpiringMap accessTokenExpiringMap;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Mock
    private ReactiveRedisService<RetrievalPayload> redisService;

    @InjectMocks
    private EmdCoreService emdCoreService;

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
        Mono<InlineResponse200> result = emdCoreService.submitMessage(requestBody);

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
                .thenReturn(Mono.error(new RuntimeException(Outcome.NO_CHANNELS_ENABLED.getValue())));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        // Act
        Mono<InlineResponse200> result = emdCoreService.submitMessage(requestBody);

        // Assert
        StepVerifier.create(result)
                .assertNext(response ->
                        response.getOutcome().equals(Outcome.NO_CHANNELS_ENABLED))
                .verifyComplete();
    }

    @Test
    void getTokenRetrievalPayloadReturnsPayload() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);
        RetrievalResponseDTO responseDTO = new RetrievalResponseDTO();
        responseDTO.setRetrievalId(retrievalId);

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(responseDTO));
        when(pnEmdIntegrationConfigs.getRetrievalPayloadCacheTtl()).thenReturn(Duration.ofMinutes(5));
        when(redisService.set(any(String.class), any(RetrievalPayload.class), any(Duration.class))).thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdCoreService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNextMatches(payload -> payload.getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void getTokenRetrievalPayloadHandlesNotFound() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.empty());

        Mono<RetrievalPayload> result = emdCoreService.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof PnEmdIntegrationNotFoundException)
                .verify();
    }

    @Test
    void getTokenRetrievalPayloadHandlesError() {
        String retrievalId = "retrievalId";

        mockAccessTokenExpiringMap();
        when(emdClient.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        Mono<RetrievalPayload> result = emdCoreService.getTokenRetrievalPayload(retrievalId);

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
}
