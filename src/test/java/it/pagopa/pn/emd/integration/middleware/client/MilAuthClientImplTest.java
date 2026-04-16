package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.api.TokenApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.ClientCredentialsGrantType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

class MilAuthClientImplTest {

    @Mock
    private TokenApi tokenApi;

    @InjectMocks
    private MilAuthClientImpl milAuthClient;

    private final String clientId = UUID.randomUUID().toString();
    private final String clientSecret = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAccessTokens_returnsAccessToken() {
        AccessToken expectedToken = new AccessToken();
        when(tokenApi.getAccessTokens(
                any(UUID.class), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(ClientCredentialsGrantType.CLIENT_CREDENTIALS),
                isNull(), isNull(), isNull(),
                any(UUID.class), isNull(), isNull(),
                any(String.class)))
                .thenReturn(Mono.just(expectedToken));

        AccessTokenRequestDto requestDto = new AccessTokenRequestDto(clientId, clientSecret);
        Mono<AccessToken> result = milAuthClient.getAccessTokens(requestDto);

        assertEquals(expectedToken, result.block());
    }

    @Test
    void getAccessTokens_handlesGenericException() {
        when(tokenApi.getAccessTokens(
                any(UUID.class), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(ClientCredentialsGrantType.CLIENT_CREDENTIALS),
                isNull(), isNull(), isNull(),
                any(UUID.class), isNull(), isNull(),
                any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("Generic Error")));

        AccessTokenRequestDto requestDto = new AccessTokenRequestDto(clientId, clientSecret);
        Mono<AccessToken> result = milAuthClient.getAccessTokens(requestDto);

        PnEmdIntegrationException exception = assertThrows(PnEmdIntegrationException.class, result::block);
        assertEquals(500, exception.getProblem().getStatus());
    }
}