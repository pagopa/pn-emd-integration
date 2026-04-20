package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.api.TokenApi;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.ClientCredentialsGrantType;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.MIL_AUTH_ERROR;

@Component
@RequiredArgsConstructor
@CustomLog
public class MilAuthClientImpl implements MilAuthClient {
    private final TokenApi tokenApi;

    @Override
    public Mono<AccessToken> getAccessTokens(AccessTokenRequestDto accessTokenRequestDto) {
        log.logInvokingExternalDownstreamService(CLIENT_NAME, "getAccessTokens");
        return tokenApi.getAccessTokens(
                UUID.randomUUID(),                                         // RequestId
                null,                                                      // Version
                null,                                                      // AcquirerId
                null,                                                      // Channel
                null,                                                      // MerchantId
                null,                                                      // TerminalId
                null,                                                      // FiscalCode
                null,                                                      // refresh_cookie
                ClientCredentialsGrantType.CLIENT_CREDENTIALS,             // grant_type
                null,                                                      // username
                null,                                                      // password
                null,                                                      // scope
                UUID.fromString(accessTokenRequestDto.getClientId()),      // client_id
                null,                                                      // return_the_refresh_token_in_the_cookie
                null,                                                      // refresh_token
                accessTokenRequestDto.getClientSecret()                    // client_secret
            )
            .onErrorResume(throwable -> {
                log.logInvokationResultDownstreamFailed(CLIENT_NAME, throwable.getMessage(), throwable);
                return Mono.error(new PnEmdIntegrationException(throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), MIL_AUTH_ERROR));
            });
    }
}
