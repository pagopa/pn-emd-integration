package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.ApiClient;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.api.SubmitApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.model.InlineResponse200;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class EmdClientImplTest {

    @Mock
    private SubmitApi submitApi;

    @InjectMocks
    private EmdClientImpl emdClient;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(submitApi.getApiClient()).thenReturn(new ApiClient());
    }

    @Test
    public void submitMessageSuccess() {
        SendMessageRequest request = new SendMessageRequest();
        String accessToken = "token";
        String requestID = "requestID";
        InlineResponse200 response = new InlineResponse200();
        response.setOutcome(Outcome.OK);

        when(submitApi.submitMessage(any(String.class), any(SendMessageRequest.class))).thenReturn(Mono.just(response));

        Mono<InlineResponse200> result = emdClient.submitMessage(request, accessToken, requestID);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    public void submitMessageError() {
        SendMessageRequest request = new SendMessageRequest();
        String accessToken = "token";
        String requestID = "requestID";

        when(submitApi.submitMessage(any(String.class), any(SendMessageRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Failed to submit message")));

        Mono<InlineResponse200> result = emdClient.submitMessage(request, accessToken, requestID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Failed to submit message"))
                .verify();
    }
}