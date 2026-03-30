package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmdCoreServiceImplTest {

    @Mock
    private EmdMessageService emdMessageService;

    @Mock
    private EmdRetrievalService emdRetrievalService;

    @Mock
    private EmdPaymentService emdPaymentService;

    @InjectMocks
    private EmdCoreServiceImpl emdCoreServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitMessageDelegatesToMessageService() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        SubmitMessage200Response response = new SubmitMessage200Response();
        response.setOutcome(Outcome.OK);

        when(emdMessageService.submitMessage(any(SendMessageRequestBody.class))).thenReturn(Mono.just(response));

        Mono<SubmitMessage200Response> result = emdCoreServiceImpl.submitMessage(requestBody);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();

        verify(emdMessageService).submitMessage(requestBody);
    }

    @Test
    void testSubmitMessagePropagatesError() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        PnEmdIntegrationException exception = new PnEmdIntegrationException("Error", 400, "Bad Request");

        when(emdMessageService.submitMessage(any(SendMessageRequestBody.class)))
                .thenReturn(Mono.error(exception));

        Mono<SubmitMessage200Response> result = emdCoreServiceImpl.submitMessage(requestBody);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationException.class)
                .verify();
    }

    @Test
    void testGetTokenRetrievalPayloadDelegatesToRetrievalService() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        when(emdRetrievalService.getTokenRetrievalPayload(retrievalId)).thenReturn(Mono.just(expectedPayload));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNext(expectedPayload)
                .verifyComplete();

        verify(emdRetrievalService).getTokenRetrievalPayload(retrievalId);
    }

    @Test
    void testGetTokenRetrievalPayloadPropagatesError() {
        String retrievalId = "retrievalId";
        PnEmdIntegrationNotFoundException exception = new PnEmdIntegrationNotFoundException("Error", "Description", "Not Found");

        when(emdRetrievalService.getTokenRetrievalPayload(retrievalId))
                .thenReturn(Mono.error(exception));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void testGetEmdRetrievalPayloadDelegatesToRetrievalService() {
        String retrievalId = "retrievalId";
        RetrievalPayload expectedPayload = new RetrievalPayload();
        expectedPayload.setRetrievalId(retrievalId);

        when(emdRetrievalService.getEmdRetrievalPayload(retrievalId)).thenReturn(Mono.just(expectedPayload));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectNext(expectedPayload)
                .verifyComplete();

        verify(emdRetrievalService).getEmdRetrievalPayload(retrievalId);
    }

    @Test
    void testGetEmdRetrievalPayloadPropagatesError() {
        String retrievalId = "retrievalId";
        PnEmdIntegrationNotFoundException exception = new PnEmdIntegrationNotFoundException("Error", "Description", "Not Found");

        when(emdRetrievalService.getEmdRetrievalPayload(retrievalId))
                .thenReturn(Mono.error(exception));

        Mono<RetrievalPayload> result = emdCoreServiceImpl.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void testGetPaymentUrlDelegatesToPaymentService() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        Integer amount = 1000;
        PaymentUrlResponse expectedResponse = new PaymentUrlResponse("http://example.com/payment");

        when(emdPaymentService.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount))
                .thenReturn(Mono.just(expectedResponse));

        Mono<PaymentUrlResponse> result = emdCoreServiceImpl.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();

        verify(emdPaymentService).getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);
    }

    @Test
    void testGetPaymentUrlPropagatesError() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        Integer amount = 1000;
        PnEmdIntegrationException exception = new PnEmdIntegrationException("Error", 400, "Bad Request");

        when(emdPaymentService.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount))
                .thenReturn(Mono.error(exception));

        Mono<PaymentUrlResponse> result = emdCoreServiceImpl.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationException.class)
                .verify();
    }
}
