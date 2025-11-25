package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@ConditionalOnProperty(
        name = "pn.emd-integration.payment.enabled",
        havingValue = "false"
)
@Service
@Slf4j
public class EmdPaymentServiceDisabled implements EmdPaymentService {
    private static final String SERVICE_DISABLED_MESSAGE = "Payment Service disabled";

    @Override
    public Mono<PaymentUrlResponse> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId, Integer amount) {
        log.info("[Payment Service disabled] - Start getPaymentUrl for retrievalId: {}, noticeCode: {}, paTaxId: {}, amount: {}", retrievalId, noticeCode, paTaxId, amount);
        return Mono.error(new PnEmdIntegrationException(
                SERVICE_DISABLED_MESSAGE,
                PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR
        ));
    }
}
