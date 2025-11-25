package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@ConditionalOnProperty(
        name = "pn.emd-integration.payment.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmdPaymentServiceImpl implements EmdPaymentService {
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Override
    public Mono<PaymentUrlResponse> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId, Integer amount) {
        log.info("getPaymentUrl for retrievalId: {}, noticeCode: {}, paTaxId: {}, amount: {}", retrievalId, noticeCode, paTaxId, amount);
        return Mono.just(new PaymentUrlResponse(createPaymentUrl(retrievalId, noticeCode, paTaxId, amount)));
    }

    private String createPaymentUrl(String retrievalId, String noticeCode, String paTaxId, Integer amount) {
        String paymentUrl = String.format("%s?retrievalId=%s&fiscalCode=%s&noticeNumber=%s",
                pnEmdIntegrationConfigs.getEmdPaymentEndpoint(),
                retrievalId,
                paTaxId,
                noticeCode);
        return (amount != null) ? String.format("%s&amount=%s", paymentUrl, amount) : paymentUrl;
    }
}
