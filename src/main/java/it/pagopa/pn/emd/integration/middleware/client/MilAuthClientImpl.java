package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.api.TokenApi;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.model.ClientCredentialsGrantType;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@CustomLog
public class MilAuthClientImpl implements MilAuthClient {
    private final TokenApi tokenApi;
    private static final String MIL_AUTH = "MIL_AUTH";

    @Override
    public Mono<AccessToken> getAccessTokens(AccessTokenRequestDto accessTokenRequestDto) {
        log.logInvokingExternalService(MIL_AUTH, "getAccessTokens");
        return tokenApi.getAccessTokens(
                ClientCredentialsGrantType.CLIENT_CREDENTIALS,
                UUID.fromString(accessTokenRequestDto.getClient_id()),
                accessTokenRequestDto.getClient_secret(),
                UUID.randomUUID());
    }
    //TODO: gestire casi di errore
}
