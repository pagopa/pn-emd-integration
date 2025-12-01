package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EmdPaymentServiceDisabledTest {
    private final EmdPaymentServiceDisabled emdPaymentServiceDisabled = new EmdPaymentServiceDisabled();

    @Test
    void getPaymentUrlServiceDisabled() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        Integer amount = 1000;

        Mono<PaymentUrlResponse> result = emdPaymentServiceDisabled.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationException.class)
                .verify();
    }

    @Test
    void getPaymentUrlServiceDisabledWithoutAmount() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";

        Mono<PaymentUrlResponse> result = emdPaymentServiceDisabled.getPaymentUrl(retrievalId, noticeCode, paTaxId, null);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationException.class)
                .verify();
    }
}
