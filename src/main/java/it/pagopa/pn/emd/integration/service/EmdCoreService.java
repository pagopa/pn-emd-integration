package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import reactor.core.publisher.Mono;

public interface EmdCoreService {
    Mono<InlineResponse200> submitMessage(SendMessageRequestBody request);
    Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId);
    Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId);
    Mono<PaymentUrlResponse> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId);

}
