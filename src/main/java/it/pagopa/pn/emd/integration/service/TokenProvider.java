package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.dto.AccessTokenRequestDto;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.model.AccessToken;
import it.pagopa.pn.emd.integration.middleware.client.MilAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

    private final MilAuthClient milAuthClient;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    public Mono<AccessToken> getAccessTokens() {
        return milAuthClient.getAccessTokens(buildRequestDto());
    }

    private AccessTokenRequestDto buildRequestDto() {
        return AccessTokenRequestDto.builder().
                client_id(pnEmdIntegrationConfigs.getMilClientId()).
                client_secret(pnEmdIntegrationConfigs.getMilClientSecret()).
                build();
    }
}