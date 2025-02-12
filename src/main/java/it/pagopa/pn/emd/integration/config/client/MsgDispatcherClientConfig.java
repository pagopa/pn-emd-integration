package it.pagopa.pn.emd.integration.config.client;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.ApiClient;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.msgdispatcher.api.SubmitApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MsgDispatcherClientConfig extends CommonBaseClient {
    private final WebClient.Builder builder;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Bean
    SubmitApi submitApi() {
        var apiClient = new ApiClient(initWebClient(this.builder));
        apiClient.setBasePath(pnEmdIntegrationConfigs.getEmdCoreBasePath());
        return new SubmitApi(apiClient);
}
}
