package it.pagopa.pn.emd.integration.cache;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emd.integration.service.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

class AccessTokenExpiringMapTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @InjectMocks
    private AccessTokenExpiringMap accessTokenExpiringMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAccessToken_returnsNewTokenWhenMapIsEmpty() {
        AccessToken expectedToken = new AccessToken();
        expectedToken.setExpiresIn(3600);
        when(tokenProvider.getAccessTokens()).thenReturn(Mono.just(expectedToken));

        Mono<AccessToken> result = accessTokenExpiringMap.getAccessToken();

        StepVerifier.create(result)
                .expectNext(expectedToken)
                .verifyComplete();
    }

    @Test
    void getAccessToken_returnsNewTokenWhenMapContainsOtherKeys() {
        AccessToken expectedToken = new AccessToken();
        expectedToken.setExpiresIn(3600);
        accessTokenExpiringMap.expiringMap.put("otherKey", expectedToken);
        when(tokenProvider.getAccessTokens()).thenReturn(Mono.just(expectedToken));

        Mono<AccessToken> result = accessTokenExpiringMap.getAccessToken();

        StepVerifier.create(result)
                .expectNext(expectedToken)
                .verifyComplete();
    }

    @Test
    void getAccessToken_returnsExistingTokenWhenNotExpired() {
        AccessToken expectedToken = new AccessToken();
        expectedToken.setExpiresIn(3600);
        accessTokenExpiringMap.expiringMap.put("milToken", expectedToken);
        accessTokenExpiringMap.expiringMap.setExpiration("milToken", 3600, TimeUnit.SECONDS);
        when(pnEmdIntegrationConfigs.getMilTokenExpirationBuffer()).thenReturn(300L);

        Mono<AccessToken> result = accessTokenExpiringMap.getAccessToken();

        StepVerifier.create(result)
                .expectNext(expectedToken)
                .verifyComplete();
    }

    @Test
    void getAccessToken_returnsNewTokenWhenExpired() {
        AccessToken expectedToken = new AccessToken();
        expectedToken.setExpiresIn(3600);
        accessTokenExpiringMap.expiringMap.put("milToken", expectedToken);
        accessTokenExpiringMap.expiringMap.setExpiration("milToken", 100, TimeUnit.SECONDS);
        when(pnEmdIntegrationConfigs.getMilTokenExpirationBuffer()).thenReturn(300L);
        when(tokenProvider.getAccessTokens()).thenReturn(Mono.just(expectedToken));

        Mono<AccessToken> result = accessTokenExpiringMap.getAccessToken();

        StepVerifier.create(result)
                .expectNext(expectedToken)
                .verifyComplete();
    }

    @Test
    void getAccessToken_returnsNewTokenWhenEntryIsAlmostExpired() {
        AccessToken expectedToken = new AccessToken();
        expectedToken.setExpiresIn(3600);
        accessTokenExpiringMap.expiringMap.put("milToken", expectedToken);
        accessTokenExpiringMap.expiringMap.setExpiration("milToken", 3, TimeUnit.SECONDS);
        when(pnEmdIntegrationConfigs.getMilTokenExpirationBuffer()).thenReturn(3000L);
        when(tokenProvider.getAccessTokens()).thenReturn(Mono.just(expectedToken));

        Mono<AccessToken> result = accessTokenExpiringMap.getAccessToken();

        StepVerifier.create(result)
                .expectNext(expectedToken)
                .verifyComplete();
    }
}