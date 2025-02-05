package it.pagopa.pn.emd.integration.rest;

import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.MessageApi;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.PaymentApi;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.CheckTppApi;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.SendMessageRequest;
import it.pagopa.pn.emd.integration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emd.integration.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PnEmdIntegrationController implements MessageApi, PaymentApi, CheckTppApi {
    private final TokenProvider tokenProvider;
    @Override
    public Mono<ResponseEntity<SendMessageResponse>> sendMessage(Mono<SendMessageRequest> sendMessageRequest, final ServerWebExchange exchange) {
        return null;
    }
    @Override
    public Mono<ResponseEntity<PaymentUrlResponse>> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId, final ServerWebExchange exchange) {
        return null;
    }
    @Override
    public Mono<ResponseEntity<RetrievalPayload>> emdCheckTPP(String retrievalId, final ServerWebExchange exchange) {
        return null;
    }
    @Override
    public Mono<ResponseEntity<RetrievalPayload>> tokenCheckTPP(String retrievalId,  final ServerWebExchange exchange) {
        return null;
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/pn-emd-integration/token-test",
            produces = { "application/json" }
    )
    public Mono<ResponseEntity<String>>  tokenTest(final ServerWebExchange exchange) {
        return tokenProvider.getToken().map(ResponseEntity.ok()::body);
    }
}
