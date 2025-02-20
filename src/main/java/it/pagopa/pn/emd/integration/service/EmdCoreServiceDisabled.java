package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@ConditionalOnProperty(
        value = "pn.emd-integration.enabled",
        havingValue = "false"
)
@Service
@Slf4j
public class EmdCoreServiceDisabled implements EmdCoreService {
    private static final String SERVICE_DISABLED_MESSAGE = "Service disabled";
    @Override
    public Mono<InlineResponse200> submitMessage(SendMessageRequestBody request) {
        log.info("[Service disabled] - Start submitMessage for request: {}", request);
        return Mono.just(new InlineResponse200(Outcome.NO_CHANNELS_ENABLED));
    }

    @Override
    public Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId) {
        log.info("[Service disabled] - Start getTokenRetrievalPayload for retrievalId: {}", retrievalId);
        return Mono.error(new PnEmdIntegrationNotFoundException(
                "Error getting retrieval payload",
                SERVICE_DISABLED_MESSAGE,
                PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR
        ));
    }

    @Override
    public Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId) {
        log.info("[Service disabled] - Start getEmdRetrievalPayload for retrievalId: {}", retrievalId);
        return Mono.error(new PnEmdIntegrationNotFoundException(
                "Error getting retrieval payload",
                SERVICE_DISABLED_MESSAGE,
                PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR
        ));
    }

    @Override
    public Mono<PaymentUrlResponse> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId) {
        log.info("[Service disabled] - Start getPaymentUrl for retrievalId: {}, noticeCode: {}, paTaxId: {}", retrievalId, noticeCode, paTaxId);
        return Mono.error(new PnEmdIntegrationException(
                SERVICE_DISABLED_MESSAGE,
                PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR
        ));
    }
}
