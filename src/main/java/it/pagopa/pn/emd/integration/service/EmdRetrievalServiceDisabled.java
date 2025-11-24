package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@ConditionalOnProperty(
        name = "pn.emd-integration.retrieval.enabled",
        havingValue = "false"
)
@Service
@Slf4j
public class EmdRetrievalServiceDisabled implements EmdRetrievalService {
    private static final String SERVICE_DISABLED_MESSAGE = "Retrieval Service disabled";

    @Override
    public Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId) {
        log.info("[Retrieval Service disabled] - Start getTokenRetrievalPayload for retrievalId: {}", retrievalId);
        return Mono.error(new PnEmdIntegrationNotFoundException(
                "Error getting retrieval payload",
                SERVICE_DISABLED_MESSAGE,
                PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR
        ));
    }

    @Override
    public Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId) {
        log.info("[Retrieval Service disabled] - Start getEmdRetrievalPayload for retrievalId: {}", retrievalId);
        return Mono.error(new PnEmdIntegrationNotFoundException(
                "Error getting retrieval payload",
                SERVICE_DISABLED_MESSAGE,
                PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR
        ));
    }
}
