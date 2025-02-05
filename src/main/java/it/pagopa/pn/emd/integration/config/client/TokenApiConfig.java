package it.pagopa.pn.emd.integration.config.client;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.ApiClient;
import it.pagopa.pn.emd.integration.generated.openapi.msclient.milauth.api.TokenApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class TokenApiConfig extends CommonBaseClient {
    private final WebClient.Builder builder;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;
    @Bean
    TokenApi tokenApi() {
        var apiClient = new ApiClient(initWebClient(this.builder));
        apiClient.setBasePath(pnEmdIntegrationConfigs.getMilBasePath());
        return new TokenApi(apiClient);
    }

}
