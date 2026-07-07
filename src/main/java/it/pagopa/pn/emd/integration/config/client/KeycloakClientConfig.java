package it.pagopa.pn.emd.integration.config.client;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientConfig extends CommonBaseClient {

    private final WebClient.Builder builder;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Bean("keycloakWebClient")
    WebClient keycloakWebClient() {
        return initWebClient(this.builder).mutate().baseUrl(pnEmdIntegrationConfigs.getKeycloakTokenEndpoint()).build();
    }
}
