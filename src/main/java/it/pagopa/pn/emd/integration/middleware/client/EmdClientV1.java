package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.SendMessageRequest;
import reactor.core.publisher.Mono;

public interface EmdClientV1 {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.EMD_CORE;
    String SUBMIT_MESSAGE_METHOD = "submitMessageV1";
    String GET_RETRIEVAL_METHOD = "getRetrievalV1";
    Mono<InlineResponse200> submitMessage(SendMessageRequest request, String token, String requestId);
    Mono<RetrievalResponseDTO> getRetrieval(String retrievalId, String accessToken);
}
