package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.commons.log.PnLogger;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SendMessageRequest;
import reactor.core.publisher.Mono;

public interface EmdClient {
    String CLIENT_NAME = PnLogger.EXTERNAL_SERVICES.EMD_CORE;
    String SUBMIT_MESSAGE_METHOD = "submitMessage";
    String GET_RETRIEVAL_METHOD = "getRetrieval";
    Mono<SubmitMessage200Response> submitMessage(SendMessageRequest request, String token, String requestId);
    Mono<RetrievalResponseDTO> getRetrieval(String retrievalId, String accessToken);
}
