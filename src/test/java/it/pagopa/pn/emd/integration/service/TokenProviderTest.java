package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emd.integration.middleware.client.MilAuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TokenProviderTest {

    @Mock
    private MilAuthClient milAuthClient;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @InjectMocks
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAccessTokens_returnsAccessToken() {
        AccessToken expectedToken = new AccessToken();
        when(milAuthClient.getAccessTokens(any(AccessTokenRequestDto.class))).thenReturn(Mono.just(expectedToken));
        when(pnEmdIntegrationConfigs.getMilClientId()).thenReturn("client_id");
        when(pnEmdIntegrationConfigs.getMilClientSecret()).thenReturn("client_secret");

        Mono<AccessToken> result = tokenProvider.getAccessTokens();

        assertEquals(expectedToken, result.block());
    }

    @Test
    void getAccessTokens_handlesError() {
        when(milAuthClient.getAccessTokens(any(AccessTokenRequestDto.class))).thenReturn(Mono.error(new RuntimeException("Error")));
        when(pnEmdIntegrationConfigs.getMilClientId()).thenReturn("client_id");
        when(pnEmdIntegrationConfigs.getMilClientSecret()).thenReturn("client_secret");

        Mono<AccessToken> result = tokenProvider.getAccessTokens();

        assertThrows(RuntimeException.class, result::block);
    }
}