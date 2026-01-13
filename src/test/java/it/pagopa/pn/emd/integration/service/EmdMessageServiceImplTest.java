package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmdMessageServiceImplTest {

    @Mock
    private EmdClientImpl emdClient;

    @Mock
    private AccessTokenExpiringMap accessTokenExpiringMap;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @InjectMocks
    private EmdMessageServiceImpl emdMessageService;

    PnEmdIntegrationConfigs.CourtesyMessageTemplate digitalMsg = new PnEmdIntegrationConfigs.CourtesyMessageTemplate();
    PnEmdIntegrationConfigs.CourtesyMessageTemplate analogMsg = new PnEmdIntegrationConfigs.CourtesyMessageTemplate();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        digitalMsg.setHeader("Header Digital");
        digitalMsg.setContent("Contenuto digitale");
        digitalMsg.setContentFileName("FileContentDigital.md");
        digitalMsg.setHeaderFileName("FileHeaderDigital.md");
        analogMsg.setHeader("Header Analog");
        analogMsg.setContent("Contenuto analogico {{schedulingAnalogDate}}");
        analogMsg.setContentFileName("FileContentAnalog.md");
        analogMsg.setHeaderFileName("FileHeaderAnalog.md");

        PnEmdIntegrationConfigs.Templates messagesTemplates = new PnEmdIntegrationConfigs.Templates();
        messagesTemplates.setAnalogMsg(analogMsg);
        messagesTemplates.setDigitalMsg(digitalMsg);

        when(pnEmdIntegrationConfigs.getMsgsTemplates()).thenReturn(messagesTemplates);
    }

    @Test
    void testSubmitMessageAnalog() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(true);
        requestBody.setOriginId("originId");
        requestBody.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.ANALOG);
        requestBody.setSchedulingAnalogDate(new Date());
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");
        InlineResponse200 response = new InlineResponse200();
        response.setOutcome(Outcome.OK);

        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
        when(emdClient.submitMessage(any(SendMessageRequest.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just(response));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        Mono<InlineResponse200> result = emdMessageService.submitMessage(requestBody);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testSubmitMessageDigitalWithoutSchedulingDate() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(true);
        requestBody.setOriginId("originId");
        requestBody.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.DIGITAL);
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");
        InlineResponse200 response = new InlineResponse200();
        response.setOutcome(Outcome.OK);

        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
        when(emdClient.submitMessage(any(SendMessageRequest.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just(response));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        Mono<InlineResponse200> result = emdMessageService.submitMessage(requestBody);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testSubmitMessageDigitalWithSchedulingDate() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(true);
        requestBody.setOriginId("originId");
        requestBody.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.DIGITAL);
        requestBody.setSchedulingAnalogDate(new Date());
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");
        InlineResponse200 response = new InlineResponse200();
        response.setOutcome(Outcome.OK);

        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
        when(emdClient.submitMessage(any(SendMessageRequest.class), any(String.class), any(String.class)))
                .thenReturn(Mono.just(response));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        Mono<InlineResponse200> result = emdMessageService.submitMessage(requestBody);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void testFailedToSubmitMessageErrorAnalogWithoutSchedulingAnalogDate() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(true);
        requestBody.setOriginId("originId");
        requestBody.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.ANALOG);
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken("token");

        when(accessTokenExpiringMap.getAccessToken()).thenReturn(Mono.just(accessToken));
        when(emdClient.submitMessage(any(SendMessageRequest.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));
        when(pnEmdIntegrationConfigs.getOriginalMessageUrl()).thenReturn("http://example.com");

        assertThrows(PnEmdIntegrationException.class,
                () -> emdMessageService.submitMessage(requestBody).block());
    }
}
