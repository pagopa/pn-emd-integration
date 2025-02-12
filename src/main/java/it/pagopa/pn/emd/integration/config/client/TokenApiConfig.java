package it.pagopa.pn.emd.integration.config.client;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.ApiClient;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.milauth.api.TokenApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TokenApiConfig extends CommonBaseClient {
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Bean
    TokenApi tokenApi() {
        var apiClient = new ApiClient(initWebClient(ApiClient.buildWebClientBuilder()));
        apiClient.setBasePath(pnEmdIntegrationConfigs.getMilBasePath());
        return new TokenApi(apiClient);
    }
}
