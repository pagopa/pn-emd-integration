package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import reactor.core.publisher.Mono;

public interface EmdRetrievalService {
    Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId);
    Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId);
}
