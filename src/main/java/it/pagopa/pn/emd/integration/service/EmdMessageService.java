package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import reactor.core.publisher.Mono;

public interface EmdMessageService {
    Mono<InlineResponse200> submitMessage(SendMessageRequestBody request);
}
