package it.pagopa.pn.emd.integration.service;


import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emd.integration.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@ConditionalOnProperty(
        value = "pn.emd-integration.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmdCoreServiceImpl implements EmdCoreService {

    private final EmdClientImpl emdClient;
    private final AccessTokenExpiringMap accessTokenExpiringMap;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;
    private final ReactiveRedisService<RetrievalPayload> redisService;

    public Mono<InlineResponse200> submitMessage(SendMessageRequestBody request) {
        log.info("Start submitMessage for request: {}", request);
        SendMessageRequest input = sendMessageRequestMap(request);
        return accessTokenExpiringMap.getAccessToken()
                .flatMap(token -> emdClient.submitMessage(input, token.getAccessToken(), UUID.randomUUID().toString()));
    }

    private SendMessageRequest sendMessageRequestMap(SendMessageRequestBody request) {
        return SendMessageRequest.builder()
                .messageId(request.getOriginId() + "_" + Utils.removePrefix(request.getInternalRecipientId()))
                .recipientId(request.getRecipientId())
                .content(pnEmdIntegrationConfigs.getCourtesyMessageContent())
                .triggerDateTime(Instant.now())
                .senderDescription(request.getSenderDescription())
                .associatedPayment(request.getAssociatedPayment())
                .messageUrl(URI.create(pnEmdIntegrationConfigs.getOriginalMessageUrl()))
                .originId(request.getOriginId())
                .channel(SendMessageRequest.ChannelEnum.SEND)
                .build();
    }

    public Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId) {
        log.info("Start getTokenRetrievalPayload for retrievalId: {}", retrievalId);
        Duration ttl = pnEmdIntegrationConfigs.getRetrievalPayloadCacheTtl();
        return getAccessTokenAndRetrievePayload(retrievalId)
                .flatMap(retrievalPayload -> redisService.set(retrievalId, retrievalPayload, ttl)
                                .thenReturn(retrievalPayload));
    }

    public Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId) {
        log.info("Start getEmdRetrievalPayload for retrievalId: {}", retrievalId);
        return redisService.get(retrievalId)
                .switchIfEmpty(Mono.defer(() -> getAccessTokenAndRetrievePayload(retrievalId)));
    }

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
        return (amount!=null)?String.format("%s&amount=%s", paymentUrl, amount):paymentUrl;
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
                .paymentButton(request.getPaymentButton())
                .originId(request.getOriginId())
                .build();
    }
}