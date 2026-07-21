package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.dto.KeycloakTokenResponseDto;
import reactor.core.publisher.Mono;

public interface KeycloakClient {
    String CLIENT_NAME = "Keycloak";
    Mono<KeycloakTokenResponseDto> getAccessToken();
}
