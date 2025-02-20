package it.pagopa.pn.emd.integration.rest;

import it.pagopa.pn.emd.integration.service.EmdCoreService;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.api.MessageApi;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.api.PaymentApi;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.api.CheckTppApi;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emd.integration.mapper.SubmitMessageResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PnEmdIntegrationController implements MessageApi, PaymentApi, CheckTppApi {
    private final EmdCoreService emdCoreService;

    @Override
    public Mono<ResponseEntity<SendMessageResponse>> sendMessage(Mono<SendMessageRequestBody> sendMessageRequest, final ServerWebExchange exchange) {
        return sendMessageRequest
                .flatMap(request -> emdCoreService.submitMessage(request)
                        .map(response -> {
                            SendMessageResponse sendMessageResponse = SubmitMessageResponseMapper.toSendMessageResponse(response);
                            return ResponseEntity.ok(sendMessageResponse);
                        }));
    }

    @Override
    public Mono<ResponseEntity<PaymentUrlResponse>> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId, final ServerWebExchange exchange) {
        return emdCoreService.getPaymentUrl(retrievalId, noticeCode, paTaxId)
                .map(ResponseEntity.ok()::body);
    }

    @Override
    public Mono<ResponseEntity<RetrievalPayload>> emdCheckTPP(String retrievalId, final ServerWebExchange exchange) {
        return emdCoreService.getEmdRetrievalPayload(retrievalId)
                .map(ResponseEntity.ok()::body);
    }

    @Override
    public Mono<ResponseEntity<RetrievalPayload>> tokenCheckTPP(String retrievalId, final ServerWebExchange exchange) {
        return emdCoreService.getTokenRetrievalPayload(retrievalId)
                .map(ResponseEntity.ok()::body);
    }
}
