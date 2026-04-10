package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.model.AccessToken;
import reactor.core.publisher.Mono;

public interface MilAuthClient {
    String CLIENT_NAME = "MIL_AUTH";
    Mono<AccessToken> getAccessTokens(AccessTokenRequestDto accessTokenRequestDto);
}
