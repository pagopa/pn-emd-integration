package it.pagopa.pn.emd.integration.middleware.client;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.dto.KeycloakTokenResponseDto;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_KEYCLOAK_TOKEN_ERROR;

@Component
@RequiredArgsConstructor
@CustomLog
public class KeycloakClientImpl implements KeycloakClient {

    private final WebClient keycloakWebClient;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Override
    public Mono<KeycloakTokenResponseDto> getAccessToken() {
        log.logInvokingExternalDownstreamService(CLIENT_NAME, "getAccessToken");
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", pnEmdIntegrationConfigs.getKeycloakClientId());
        formData.add("client_secret", pnEmdIntegrationConfigs.getKeycloakClientSecret());
        return keycloakWebClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(KeycloakTokenResponseDto.class)
                .onErrorResume(throwable -> {
                    log.logInvokationResultDownstreamFailed(CLIENT_NAME, throwable.getMessage(), throwable);
                    return Mono.error(new PnEmdIntegrationException(throwable.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), PN_EMD_INTEGRATION_KEYCLOAK_TOKEN_ERROR));
                });
    }
}
