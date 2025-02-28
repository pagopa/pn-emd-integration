package it.pagopa.pn.emd.integration.cache;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emd.integration.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenExpiringMap {


    private final TokenProvider tokenProvider;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;
    private static final String TOKEN_KEY = "milToken";

    protected ExpiringMap<String, AccessToken> expiringMap = ExpiringMap.builder()
            .asyncExpirationListener((tokenKey, entry) -> log.info("A token has expired"))
            .variableExpiration()
            .build();

    public Mono<AccessToken> getAccessToken() {
        if (expiringMap.isEmpty() || !expiringMap.containsKey(TOKEN_KEY)) {
            return retrieveNewAccessToken();
        }
        try {
            long expiration = expiringMap.getExpectedExpiration(TOKEN_KEY);
            if (expiration <= pnEmdIntegrationConfigs.getMilTokenExpirationBuffer()) {
                return retrieveNewAccessToken();
            } else {
                log.info("Using cached Access Token");
                return Mono.just(expiringMap.get(TOKEN_KEY));
            }
        } catch (NoSuchElementException e) {
            // This should never happen
            return retrieveNewAccessToken();
        }
    }


    private Mono<AccessToken> retrieveNewAccessToken() {
        log.info("New Access Token requested");
        return tokenProvider.getAccessTokens()
                .map(accessToken -> {
                    expiringMap.put(TOKEN_KEY, accessToken);
                    expiringMap.setExpiration(TOKEN_KEY, accessToken.getExpiresIn(), TimeUnit.SECONDS);
                    log.debug("New Access Token expires in {}s", accessToken.getExpiresIn());
                    return accessToken;
                });
    }

}
