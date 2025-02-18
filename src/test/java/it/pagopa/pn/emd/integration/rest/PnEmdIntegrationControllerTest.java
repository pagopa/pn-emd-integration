package it.pagopa.pn.emd.integration.rest;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emd.integration.service.EmdCoreService;
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

class PnEmdIntegrationControllerTest {

    @Mock
    private EmdCoreService emdCoreService;

    @InjectMocks
    private PnEmdIntegrationController pnEmdIntegrationController;

    @BeforeEach
    void setUp() {
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

        when(emdCoreService.submitMessage(any(SendMessageRequestBody.class)))
                .thenReturn(Mono.just(new InlineResponse200().outcome(Outcome.OK)));

        Mono<ResponseEntity<SendMessageResponse>> response = pnEmdIntegrationController.sendMessage(Mono.just(requestBody), null);

        StepVerifier.create(response)
                .expectNextMatches(entity -> entity.getStatusCode().is2xxSuccessful() && entity.getBody().getOutcome() == SendMessageResponse.OutcomeEnum.OK)
                .verifyComplete();
    }

    @Test
    void tokenCheckTPPReturnsPayload() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        when(emdCoreService.getTokenRetrievalPayload(retrievalId)).thenReturn(Mono.just(expectedPayload));

        Mono<ResponseEntity<RetrievalPayload>> result = pnEmdIntegrationController.tokenCheckTPP(retrievalId, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful() && response.getBody().getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void tokenCheckTPPHandlesNotFound() {
        String retrievalId = "retrievalId";

        when(emdCoreService.getTokenRetrievalPayload(retrievalId)).thenReturn(Mono.error(new PnEmdIntegrationNotFoundException("Not Found", null, null)));

        Mono<ResponseEntity<RetrievalPayload>> result = pnEmdIntegrationController.tokenCheckTPP(retrievalId, null);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof PnEmdIntegrationNotFoundException && throwable.getMessage().equals("Not Found"))
                .verify();
    }

    @Test
    void emdCheckTPPReturnsPayload() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        when(emdCoreService.getEmdRetrievalPayload(retrievalId)).thenReturn(Mono.just(expectedPayload));

        Mono<ResponseEntity<RetrievalPayload>> result = pnEmdIntegrationController.emdCheckTPP(retrievalId, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode().is2xxSuccessful() && response.getBody().getRetrievalId().equals(retrievalId))
                .verifyComplete();
    }

    @Test
    void emdCheckTPPHandlesNotFound() {
        String retrievalId = "retrievalId";

        when(emdCoreService.getEmdRetrievalPayload(retrievalId)).thenReturn(Mono.error(new PnEmdIntegrationNotFoundException("Not Found", null, null)));

        Mono<ResponseEntity<RetrievalPayload>> result = pnEmdIntegrationController.emdCheckTPP(retrievalId, null);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof PnEmdIntegrationNotFoundException && throwable.getMessage().equals("Not Found"))
                .verify();
    }
}