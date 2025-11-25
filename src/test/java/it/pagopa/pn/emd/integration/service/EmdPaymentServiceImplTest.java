package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

class EmdPaymentServiceImplTest {

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @InjectMocks
    private EmdPaymentServiceImpl emdPaymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPaymentUrlReturnsCorrectUrl() {
        String retrievalId = "retrievalId";
        String noticeCode = "noticeCode";
        String paTaxId = "paTaxId";
        Integer amount = 1000;
        String emdPaymentEndpoint = "http://example.com/emd_endpoint";

        when(pnEmdIntegrationConfigs.getEmdPaymentEndpoint()).thenReturn(emdPaymentEndpoint);

        Mono<PaymentUrlResponse> result = emdPaymentService.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);

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

        Mono<PaymentUrlResponse> result = emdPaymentService.getPaymentUrl(retrievalId, noticeCode, paTaxId, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getPaymentUrl().equals("http://example.com/emd_endpoint?retrievalId=retrievalId&fiscalCode=paTaxId&noticeNumber=noticeCode"))
                .verifyComplete();
    }
}
