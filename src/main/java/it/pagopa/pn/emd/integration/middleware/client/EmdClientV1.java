package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.SendMessageRequest;
import reactor.core.publisher.Mono;

public interface EmdClientV1 {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.EMD_CORE;
    String SUBMIT_MESSAGE_METHOD = "submitMessage";
    Mono<InlineResponse200> submitMessage(SendMessageRequest request, String token, String requestId);
}
