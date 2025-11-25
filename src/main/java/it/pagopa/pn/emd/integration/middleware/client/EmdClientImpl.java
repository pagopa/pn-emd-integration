package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.api.PaymentApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.api.SubmitApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.InlineResponse200;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@CustomLog
public class EmdClientImpl implements EmdClient{
    private final SubmitApi submitApi;
    private final PaymentApi paymentApi;
    private static final String ACCEPT_LANGUAGE = "it-IT";

    @Override
    public Mono<InlineResponse200> submitMessage(SendMessageRequest request, String accessToken, String requestID) {
        log.logInvokingExternalDownstreamService(CLIENT_NAME, SUBMIT_MESSAGE_METHOD);
        submitApi.getApiClient().setBearerToken(accessToken);
        return submitApi.submitMessage(requestID, request)
                .doOnError(throwable -> log.logInvokationResultDownstreamFailed(SUBMIT_MESSAGE_METHOD, throwable.getMessage()))
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
        log.logInvokingExternalDownstreamService(CLIENT_NAME, GET_RETRIEVAL_METHOD);
        paymentApi.getApiClient().setBearerToken(accessToken);
        return paymentApi.getRetrieval(ACCEPT_LANGUAGE, retrievalId)
                .doOnError(throwable -> log.logInvokationResultDownstreamFailed(GET_RETRIEVAL_METHOD, throwable.getMessage()))
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
