package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.api.SubmitApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.InlineResponse200;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.emd.integration.middleware.client.MilAuthClient.CLIENT_NAME;

@Component
@RequiredArgsConstructor
@CustomLog
public class EmdClientImpl implements EmdClient{
    private final SubmitApi submitApi;

    @Override
    public Mono<InlineResponse200> submitMessage(SendMessageRequest request, String accessToken, String requestID) {
        log.logInvokingExternalService(CLIENT_NAME, "submitMessage");
        submitApi.getApiClient().setBearerToken(accessToken);
        return submitApi.submitMessage(requestID, request)
                .doOnError(
                        throwable -> log.logInvokationResultDownstreamFailed("submitMessage", throwable.getMessage()));
    }
}
