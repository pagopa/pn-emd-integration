package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import reactor.core.publisher.Mono;

public interface EmdMessageService {
    Mono<SubmitMessage200Response> submitMessage(SendMessageRequestBody request);
}
