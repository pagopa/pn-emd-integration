package it.pagopa.pn.emd.integration.service;


import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.PaymentUrlResponse;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmdCoreServiceImpl implements EmdCoreService {
    private final EmdMessageService emdMessageService;
    private final EmdRetrievalService emdRetrievalService;
    private final EmdPaymentService emdPaymentService;

    @Override
    public Mono<SubmitMessage200Response> submitMessage(SendMessageRequestBody request) {
        log.debug("EmdCoreService delegating submitMessage to EmdMessageService");
        return emdMessageService.submitMessage(request);
    }

    @Override
    public Mono<RetrievalPayload> getTokenRetrievalPayload(String retrievalId) {
        log.debug("EmdCoreService delegating getTokenRetrievalPayload to EmdRetrievalService");
        return emdRetrievalService.getTokenRetrievalPayload(retrievalId);
    }

    @Override
    public Mono<RetrievalPayload> getEmdRetrievalPayload(String retrievalId) {
        log.debug("EmdCoreService delegating getEmdRetrievalPayload to EmdRetrievalService");
        return emdRetrievalService.getEmdRetrievalPayload(retrievalId);
    }

    @Override
    public Mono<PaymentUrlResponse> getPaymentUrl(String retrievalId, String noticeCode, String paTaxId, Integer amount) {
        log.debug("EmdCoreService delegating getPaymentUrl to EmdPaymentService");
        return emdPaymentService.getPaymentUrl(retrievalId, noticeCode, paTaxId, amount);
    }
}