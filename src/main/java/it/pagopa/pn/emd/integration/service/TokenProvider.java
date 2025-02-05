package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.middleware.client.MilAuthClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

    //private static final String TOKEN_KEY = "emdAccessToken";
    private CachedToken cachedToken;
    private final MilAuthClient milAuthClient;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    public Mono<String> getToken() {;
        if (cachedToken != null && !cachedToken.isExpired()) {
            log.info("Token is still valid, returning cached token");
            return Mono.just(cachedToken.getToken());
        }

        return milAuthClient.getAccessTokens(AccessTokenRequestDto.builder().
                        client_id(pnEmdIntegrationConfigs.getMilClientId()).
                        client_secret(pnEmdIntegrationConfigs.getMilClientSecret()).
                        build())
                .flatMap(response -> {
                    if (response != null) {
                        long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(response.getExpiresIn());
                        this.cachedToken = new CachedToken(response.getAccessToken(), expirationTime);
                        return Mono.just(cachedToken.getToken());
                    }
                    return Mono.error(new RuntimeException("Failed to retrieve token from pn-emd-core"));
                });
    }

    @Setter
    @Getter
    private class CachedToken {
        private final String token;
        private final long expirationTime;

        public CachedToken(String token, long expirationTime) {
            this.token = token;
            this.expirationTime = expirationTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime-(pnEmdIntegrationConfigs.getMilTokenExpirationBuffer());
        }
    }

    @Setter
    @Getter
    private class TokenResponse {
        private String token;
        private long expiresIn;

    }
}