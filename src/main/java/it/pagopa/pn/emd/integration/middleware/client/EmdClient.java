package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.SendMessageRequest;
import reactor.core.publisher.Mono;

public interface EmdClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.EMD_CORE;
    Mono<InlineResponse200> submitMessage(SendMessageRequest request, String token, String requestId);
}
