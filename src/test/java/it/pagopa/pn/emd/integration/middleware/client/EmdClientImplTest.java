package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.ApiClient;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.api.PaymentApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.api.SubmitApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.RetrievalResponseDTO;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.InlineResponse200;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_GET_RETRIEVAL_PAYLOAD_ERROR;
import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_SEND_MESSAGE_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmdClientImplTest {

    @Mock
    private SubmitApi submitApi;

    @Mock
    private PaymentApi paymentApi;

    @InjectMocks
    private EmdClientImpl emdClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(submitApi.getApiClient()).thenReturn(new ApiClient());
        when(paymentApi.getApiClient()).thenReturn(new ApiClient());
    }

    @Test
    void submitMessageSuccess() {
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
    void submitMessageError() {
        SendMessageRequest request = new SendMessageRequest();
        String accessToken = "token";
        String requestID = "requestID";

        when(submitApi.submitMessage(any(String.class), any(SendMessageRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Failed to submit message")));

        Mono<InlineResponse200> result = emdClient.submitMessage(request, accessToken, requestID);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof PnEmdIntegrationException &&
                        ((PnEmdIntegrationException) throwable).getCode().equals(PN_EMD_INTEGRATION_SEND_MESSAGE_ERROR))
                .verify();
    }

    @Test
    void getRetrievalSuccess() {
        String retrievalId = "retrievalId";
        String accessToken = "token";
        RetrievalResponseDTO expectedResponse = new RetrievalResponseDTO();

        when(paymentApi.getRetrieval(any(String.class), any(String.class))).thenReturn(Mono.just(expectedResponse));

        Mono<RetrievalResponseDTO> result = emdClient.getRetrieval(retrievalId, accessToken);

        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    void getRetrievalNotFound() {
        String retrievalId = "retrievalId";
        String accessToken = "token";

        when(paymentApi.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new WebClientResponseException(404, "Not Found", null, null, null, null)));

        Mono<RetrievalResponseDTO> result = emdClient.getRetrieval(retrievalId, accessToken);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void getRetrievalHandlesError() {
        String retrievalId = "retrievalId";
        String accessToken = "token";

        when(paymentApi.getRetrieval(any(String.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        Mono<RetrievalResponseDTO> result = emdClient.getRetrieval(retrievalId, accessToken);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof PnEmdIntegrationException &&
                        ((PnEmdIntegrationException) throwable).getCode().equals(PN_EMD_INTEGRATION_GET_RETRIEVAL_PAYLOAD_ERROR))
                .verify();
    }
}