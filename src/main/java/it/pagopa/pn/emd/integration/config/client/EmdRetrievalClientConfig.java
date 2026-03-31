package it.pagopa.pn.emd.integration.config.client;

import it.pagopa.pn.commons.pnclients.CommonBaseClient;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdpayment.ApiClient;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdpayment.api.PaymentApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EmdRetrievalClientConfig extends CommonBaseClient {

    private final WebClient.Builder builder;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Bean("emdPaymentApi")
    PaymentApi paymentApi() {
        var apiClient = new ApiClient(initWebClient(this.builder));
        apiClient.setBasePath(pnEmdIntegrationConfigs.getEmdCorePaymentBasePath());
        return new PaymentApi(apiClient);
    }
}
