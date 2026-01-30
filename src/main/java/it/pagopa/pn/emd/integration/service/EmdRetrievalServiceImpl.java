package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@ConditionalOnProperty(
        name = "pn.emd-integration.retrieval.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmdRetrievalServiceImpl implements EmdRetrievalService {
    private final EmdClientImpl emdClient;
    private final AccessTokenExpiringMap accessTokenExpiringMap;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;
    private final ReactiveRedisService<RetrievalPayload> redisService;

    @Override
    public Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId) {
        log.info("Start getTokenRetrievalPayload for retrievalId: {}", retrievalId);
        Duration ttl = pnEmdIntegrationConfigs.getRetrievalPayloadCacheTtl();
        return getAccessTokenAndRetrievePayload(retrievalId)
                .flatMap(retrievalPayload -> redisService.set(retrievalId, retrievalPayload, ttl)
                        .thenReturn(retrievalPayload));
    }

    @Override
    public Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId) {
        log.info("Start getEmdRetrievalPayload for retrievalId: {}", retrievalId);
        return redisService.get(retrievalId)
                .switchIfEmpty(Mono.defer(() -> getAccessTokenAndRetrievePayload(retrievalId)));
    }

    private Mono<RetrievalPayload> getAccessTokenAndRetrievePayload(String retrievalId) {
        log.info("Retrieving payload for retrievalId: {}", retrievalId);
        return accessTokenExpiringMap.getAccessToken()
                .flatMap(token -> emdClient.getRetrieval(retrievalId, token.getAccessToken()))
                .switchIfEmpty(
                        Mono.error(
                                new PnEmdIntegrationNotFoundException(
                                        "Error getting retrieval payload",
                                        "Retrieval payload not found or expired",
                                        PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_RETRIEVAL_PAYLOAD_MISSING_OR_EXPIRED
                                )
                        )
                )
                .map(this::mapToRetrievalPayload);
    }

    private RetrievalPayload mapToRetrievalPayload(RetrievalResponseDTO request) {
        return RetrievalPayload.builder()
                .retrievalId(request.getRetrievalId())
                .tppId(request.getTppId())
                .deeplink(request.getDeeplink())
                .pspDenomination(request.getPspDenomination())
                .originId(request.getOriginId())
                .isPaymentEnabled(request.getIsPaymentEnabled() != null && request.getIsPaymentEnabled())
                .build();
    }
}
