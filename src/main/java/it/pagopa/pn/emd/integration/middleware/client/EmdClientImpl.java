package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdmessage.api.SubmitApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdmessage.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdmessage.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdpayment.api.PaymentApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdpayment.model.RetrievalResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmdClientImpl implements EmdClient {
    private final SubmitApi submitApi;
    private final PaymentApi paymentApi;
    private static final String ACCEPT_LANGUAGE = "it-IT";

    @Override
    public Mono<SubmitMessage200Response> submitMessage(SendMessageRequest request, String accessToken, String requestID) {
        log.info("Invoking {} - {}", CLIENT_NAME, SUBMIT_MESSAGE_METHOD);
        submitApi.getApiClient().setAccessToken(accessToken);
        return submitApi.submitMessage(requestID, request)
                .doOnError(throwable -> log.error("Invocation failed for {} - {}: {}", CLIENT_NAME, SUBMIT_MESSAGE_METHOD, throwable.getMessage(), throwable))
                .onErrorMap(throwable -> {
                    throw new PnEmdIntegrationException(
                            "Error sending message to EMD",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SEND_MESSAGE_ERROR
                    );
                });
    }

    @Override
    public Mono<RetrievalResponseDTO> getRetrieval(String retrievalId, String accessToken) {
        log.info("Invoking {} - {}", CLIENT_NAME, GET_RETRIEVAL_METHOD);
        paymentApi.getApiClient().setAccessToken(accessToken);
        return paymentApi.getRetrieval(ACCEPT_LANGUAGE, retrievalId)
                .doOnNext(response -> log.debug("Retrieved payload from EMD: {}", response))
                .doOnError(throwable -> log.error("Invocation failed for {} - {}: {}", CLIENT_NAME, GET_RETRIEVAL_METHOD, throwable.getMessage(), throwable))
                .onErrorResume(this::isNotFoundException, e -> Mono.empty())
                .onErrorMap(throwable -> {
                    throw new PnEmdIntegrationException(
                            "Error retrieving payload from EMD",
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_GET_RETRIEVAL_PAYLOAD_ERROR
                    );
                });
    }

    private boolean isNotFoundException(Throwable e) {
        if (!(e instanceof WebClientResponseException webClientResponseException)) {
            return false;
        }
        return webClientResponseException.getStatusCode().equals(HttpStatus.NOT_FOUND);
    }
}
