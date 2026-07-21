package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.dto.KeycloakTokenResponseDto;
import it.pagopa.pn.emd.integration.middleware.client.KeycloakClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenProvider {

    private final KeycloakClient keycloakClient;

    public Mono<KeycloakTokenResponseDto> getAccessTokens() {
        return keycloakClient.getAccessToken();
    }
}