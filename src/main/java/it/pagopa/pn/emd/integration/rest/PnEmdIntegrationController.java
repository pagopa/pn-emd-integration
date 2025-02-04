package it.pagopa.pn.emd.integration.rest;

import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.MessageApi;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.PaymentApi;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.CheckTppApi;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.SendMessageRequest;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.SendMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class PnEmdIntegrationController implements MessageApi, PaymentApi, CheckTppApi {

    public Mono<ResponseEntity<SendMessageResponse>> sendMessage(Mono<SendMessageRequest> sendMessageRequest, final ServerWebExchange exchange) {
        return null;
    }

    public Mono<ResponseEntity<PaymentUrlResponse>> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId, final ServerWebExchange exchange) {
        return null;
    }

    public Mono<ResponseEntity<RetrievalPayload>> emdCheckTPP(String retrievalId, final ServerWebExchange exchange) {
        return null;
    }

    public Mono<ResponseEntity<RetrievalPayload>> tokenCheckTPP(String retrievalId,  final ServerWebExchange exchange) {
        return null;
    }
}
