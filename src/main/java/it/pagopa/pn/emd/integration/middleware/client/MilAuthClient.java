package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.model.AccessToken;
import reactor.core.publisher.Mono;

public interface MilAuthClient {
    Mono<AccessToken> getAccessTokens(AccessTokenRequestDto accessTokenRequestDto);
}
