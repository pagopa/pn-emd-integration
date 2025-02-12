package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.NO_CHANNELS_ENABLED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MsgDispatcherImplTest {

    @Mock
    private EmdClientImpl emdClient;

    @Mock
    private AccessTokenExpiringMap accessTokenExpiringMap;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @InjectMocks
    private MsgDispatcherImpl msgDispatcher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSubmitMessage() {
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
        Mono<InlineResponse200> result = msgDispatcher.submitMessage(requestBody);

        // Assert
        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    public void testSubmitMessageError() {
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
                .thenReturn(Mono.error(new RuntimeException(NO_CHANNELS_ENABLED)));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        // Act
        Mono<InlineResponse200> result = msgDispatcher.submitMessage(requestBody);

        // Assert
        StepVerifier.create(result)
                .assertNext(response ->
                        response.getOutcome().equals(NO_CHANNELS_ENABLED))
                .verifyComplete();
    }
}
