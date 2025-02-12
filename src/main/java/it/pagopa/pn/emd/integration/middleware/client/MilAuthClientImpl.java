package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.api.TokenApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.MIL_AUTH_ERROR;

@Component
@RequiredArgsConstructor
@CustomLog
public class MilAuthClientImpl implements MilAuthClient {
    private final TokenApi tokenApi;
    private static final String GRANT_TYPE = "client_credentials";

    @Override
    public Mono<AccessToken> getAccessTokens(AccessTokenRequestDto accessTokenRequestDto) {
        log.logInvokingExternalService(CLIENT_NAME, "getAccessTokens");
        return tokenApi.getAccessTokens(
                GRANT_TYPE,
                UUID.fromString(accessTokenRequestDto.getClientId()),
                accessTokenRequestDto.getClientSecret(),
                UUID.randomUUID()
            )
            .doOnError(throwable -> {
                log.logInvokationResultDownstreamFailed(CLIENT_NAME, throwable.getMessage());
                if (throwable instanceof WebClientResponseException e) {
                    throw new PnEmdIntegrationException(e.getMessage(), e.getRawStatusCode(), MIL_AUTH_ERROR);
                }

                throw new PnEmdIntegrationException(throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), MIL_AUTH_ERROR);
            });
    }
}
