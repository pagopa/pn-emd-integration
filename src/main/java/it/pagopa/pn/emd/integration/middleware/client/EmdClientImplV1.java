package it.pagopa.pn.emd.integration.middleware.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.api.PaymentApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.api.SubmitApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.SendMessageRequest;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@CustomLog
public class EmdClientImplV1 implements EmdClientV1 {

    private final SubmitApi submitApi;
    private final PaymentApi paymentApi;
    private static final String ACCEPT_LANGUAGE = "it-IT";
    private final ObjectMapper objectMapper;
    @Override
    public Mono<InlineResponse200> submitMessage(SendMessageRequest request, String accessToken, String requestID) {
        log.logInvokingExternalService(CLIENT_NAME, SUBMIT_MESSAGE_METHOD);
        submitApi.getApiClient().setBearerToken(accessToken);
        return submitApi.submitMessage(requestID, request)
                        .map( responseV1 -> objectMapper.convertValue(responseV1, InlineResponse200.class))
                        .doOnError(throwable -> log.logInvokationResultDownstreamFailed(SUBMIT_MESSAGE_METHOD, throwable.getMessage()))
                        .onErrorMap(throwable -> {
                            throw new PnEmdIntegrationException(
                                    "Error sending message to EMD",
                                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SEND_MESSAGE_ERROR
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