package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.dto.KeycloakTokenResponseDto;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_KEYCLOAK_TOKEN_ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakClientImplTest {

    @Mock
    private WebClient keycloakWebClient;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    private KeycloakClientImpl keycloakClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        keycloakClient = new KeycloakClientImpl(keycloakWebClient, pnEmdIntegrationConfigs);

        when(pnEmdIntegrationConfigs.getKeycloakClientId()).thenReturn("test-client-id");
        when(pnEmdIntegrationConfigs.getKeycloakClientSecret()).thenReturn("test-client-secret");
    }

    @SuppressWarnings("unchecked")
    private WebClient.RequestHeadersSpec<?> stubWebClientChain(WebClient.ResponseSpec responseSpec) {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);

        when(keycloakWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(any())).thenReturn(requestBodyUriSpec);
        doReturn(requestHeadersSpec).when(requestBodyUriSpec).body(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        return requestHeadersSpec;
    }

    @Test
    void getAccessToken_returnsTokenOnSuccess() {
        KeycloakTokenResponseDto expectedDto = KeycloakTokenResponseDto.builder()
                .accessToken("my-access-token")
                .tokenType("Bearer")
                .expiresIn(300)
                .build();

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(KeycloakTokenResponseDto.class)).thenReturn(Mono.just(expectedDto));
        stubWebClientChain(responseSpec);

        StepVerifier.create(keycloakClient.getAccessToken())
                .expectNextMatches(dto ->
                        "my-access-token".equals(dto.getAccessToken()) &&
                        Integer.valueOf(300).equals(dto.getExpiresIn()))
                .verifyComplete();
    }

    @Test
    void getAccessToken_wrapsErrorInPnEmdIntegrationException() {
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(responseSpec.bodyToMono(KeycloakTokenResponseDto.class))
                .thenReturn(Mono.error(new RuntimeException("Connection refused")));
        stubWebClientChain(responseSpec);

        StepVerifier.create(keycloakClient.getAccessToken())
                .expectErrorMatches(throwable ->
                        throwable instanceof PnEmdIntegrationException &&
                        ((PnEmdIntegrationException) throwable).getCode().equals(PN_EMD_INTEGRATION_KEYCLOAK_TOKEN_ERROR) &&
                        ((PnEmdIntegrationException) throwable).getProblem().getStatus() == 500)
                .verify();
    }
}
