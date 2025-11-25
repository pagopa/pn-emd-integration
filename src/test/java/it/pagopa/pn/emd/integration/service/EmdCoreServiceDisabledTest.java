package it.pagopa.pn.emd.integration.service;


import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EmdCoreServiceDisabledTest {
    private final EmdCoreServiceDisabled emdCoreServiceDisabled = new EmdCoreServiceDisabled();

    @Test
    void submitMessageReturnsNoChannelsEnabled() {
        Mono<InlineResponse200> result = emdCoreServiceDisabled.submitMessage(new SendMessageRequestBody());

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getOutcome() == Outcome.NO_CHANNELS_ENABLED)
                .verifyComplete();
    }

    @Test
    void getTokenRetrievalPayloadServiceDisabled() {
        String retrievalId = "retrievalId";

        Mono<RetrievalPayload> result = emdCoreServiceDisabled.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void getEmdRetrievalPayloadServiceDisabled() {
        String retrievalId = "retrievalId";

        Mono<RetrievalPayload> result = emdCoreServiceDisabled.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void getPaymentUrlServiceDisabled() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        Integer amount = 1000;

        Mono<PaymentUrlResponse> result = emdCoreServiceDisabled.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationException.class)
                .verify();
    }

    @Test
    void getPaymentUrlServiceDisabledWithoutAmount() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";

        Mono<PaymentUrlResponse> result = emdCoreServiceDisabled.getPaymentUrl(retrievalId, noticeCode, paTaxId, null);

        StepVerifier.create(result)
                    .expectError(PnEmdIntegrationException.class)
                    .verify();
    }
}