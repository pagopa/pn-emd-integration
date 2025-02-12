package it.pagopa.pn.emd.integration.rest;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.InlineResponse200;
import it.pagopa.pn.emd.integration.service.MsgDispatcherImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PnEmdIntegrationControllerTest {

    @Mock
    private MsgDispatcherImpl msgDispatcherImpl;

    @InjectMocks
    private PnEmdIntegrationController pnEmdIntegrationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMessage() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setOriginId("originId");
        requestBody.setRecipientId("recipientId");
        requestBody.setSenderDescription("senderDescription");
        requestBody.setAssociatedPayment(false);

        SendMessageResponse sendMessageResponse = new SendMessageResponse();
        sendMessageResponse.setOutcome(SendMessageResponse.OutcomeEnum.OK);

        when(msgDispatcherImpl.submitMessage(any(SendMessageRequestBody.class)))
                .thenReturn(Mono.just(new InlineResponse200().outcome(Outcome.OK)));

        Mono<ResponseEntity<SendMessageResponse>> response = pnEmdIntegrationController.sendMessage(Mono.just(requestBody), null);

        StepVerifier.create(response)
                .expectNextMatches(entity -> entity.getStatusCode().is2xxSuccessful() && entity.getBody().getOutcome() == SendMessageResponse.OutcomeEnum.OK)
                .verifyComplete();
    }


}