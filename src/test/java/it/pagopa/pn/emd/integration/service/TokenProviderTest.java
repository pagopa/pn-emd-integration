package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.dto.KeycloakTokenResponseDto;
import it.pagopa.pn.emd.integration.middleware.client.KeycloakClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TokenProviderTest {

    @Mock
    private KeycloakClient keycloakClient;

    @InjectMocks
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAccessTokens_returnsAccessToken() {
        KeycloakTokenResponseDto expectedToken = new KeycloakTokenResponseDto();
        when(keycloakClient.getAccessToken()).thenReturn(Mono.just(expectedToken));

        Mono<KeycloakTokenResponseDto> result = tokenProvider.getAccessTokens();

        assertEquals(expectedToken, result.block());
    }

    @Test
    void getAccessTokens_handlesError() {
        when(keycloakClient.getAccessToken()).thenReturn(Mono.error(new RuntimeException("Error")));

        Mono<KeycloakTokenResponseDto> result = tokenProvider.getAccessTokens();

        assertThrows(RuntimeException.class, result::block);
    }
}