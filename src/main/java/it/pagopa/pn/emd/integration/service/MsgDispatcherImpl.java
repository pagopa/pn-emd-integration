package it.pagopa.pn.emd.integration.service;


import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emd.integration.utils.PnEmdIntegrationCostants;
import it.pagopa.pn.emd.integration.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MsgDispatcherImpl {

    private final EmdClientImpl emdClient;
    private final AccessTokenExpiringMap accessTokenExpiringMap;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    public Mono<InlineResponse200> submitMessage(SendMessageRequestBody request) {
        SendMessageRequest input=sendMessageRequestMap(request);
        return accessTokenExpiringMap.getAccessToken()
                .flatMap(token -> emdClient.submitMessage(input,token.getAccessToken(), UUID.randomUUID().toString()))
                .onErrorResume(throwable ->
                        {
                            log.error("Exception caught, fallback to NO_CHANNELS_ENABLED", throwable);
                            return Mono.just(InlineResponse200.builder().outcome(Outcome.NO_CHANNELS_ENABLED).build());
                        });
    }

    private SendMessageRequest sendMessageRequestMap(SendMessageRequestBody request) {
        return SendMessageRequest.builder()
                .messageId(request.getOriginId()+"_"+ Utils.removePrefix(request.getInternalRecipientId()))
                .recipientId(request.getRecipientId())
                .content(PnEmdIntegrationCostants.COURTESY_MESSAGE_CONTENT)
                .triggerDateTime(Instant.now())
                .senderDescription(request.getSenderDescription())
                .associatedPayment(request.getAssociatedPayment())
                .messageUrl(URI.create(pnEmdIntegrationConfigs.getOriginalMessageUrl()))
                .originId(request.getOriginId())
                .channel(SendMessageRequest.ChannelEnum.SEND)
                .build();
    }
}