package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImplV1;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class EmdCoreServiceConditionalTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConditionEvaluationReportLoggingListener())
            .withUserConfiguration(TestConfig.class);

    @Test
    void emdMessageServiceImplIsChosen() {
        contextRunner
                .withUserConfiguration(EmdMessageServiceImpl.class, EmdMessageServiceDisabled.class)
                .withPropertyValues("pn.emd-integration.message.enabled=true")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdMessageServiceImpl.class),
                        () -> assertThat(context).doesNotHaveBean(EmdMessageServiceDisabled.class)));
    }

    @Test
    void emdMessageServiceDisabledIsChosen() {
        contextRunner
                .withUserConfiguration(EmdMessageServiceImpl.class, EmdMessageServiceDisabled.class)
                .withPropertyValues("pn.emd-integration.message.enabled=false")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdMessageServiceDisabled.class),
                        () -> assertThat(context).doesNotHaveBean(EmdMessageServiceImpl.class)));
    }

    @Test
    void emdRetrievalServiceImplIsChosen() {
        contextRunner
                .withUserConfiguration(EmdRetrievalServiceImpl.class, EmdRetrievalServiceDisabled.class)
                .withPropertyValues("pn.emd-integration.retrieval.enabled=true")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdRetrievalServiceImpl.class),
                        () -> assertThat(context).doesNotHaveBean(EmdRetrievalServiceDisabled.class)));
    }

    @Test
    void emdRetrievalServiceDisabledIsChosen() {
        contextRunner
                .withUserConfiguration(EmdRetrievalServiceImpl.class, EmdRetrievalServiceDisabled.class)
                .withPropertyValues("pn.emd-integration.retrieval.enabled=false")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdRetrievalServiceDisabled.class),
                        () -> assertThat(context).doesNotHaveBean(EmdRetrievalServiceImpl.class)));
    }

    @Test
    void emdPaymentServiceImplIsChosen() {
        contextRunner
                .withUserConfiguration(EmdPaymentServiceImpl.class, EmdPaymentServiceDisabled.class)
                .withPropertyValues("pn.emd-integration.payment.enabled=true")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdPaymentServiceImpl.class),
                        () -> assertThat(context).doesNotHaveBean(EmdPaymentServiceDisabled.class)));
    }

    @Test
    void emdPaymentServiceDisabledIsChosen() {
        contextRunner
                .withUserConfiguration(EmdPaymentServiceImpl.class, EmdPaymentServiceDisabled.class)
                .withPropertyValues("pn.emd-integration.payment.enabled=false")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdPaymentServiceDisabled.class),
                        () -> assertThat(context).doesNotHaveBean(EmdPaymentServiceImpl.class)));
    }

    @Configuration
    protected static class TestConfig {
        @Bean
        public EmdClientImpl emdClientDependency() {
            return Mockito.mock(EmdClientImpl.class);
        }

        @Bean
        public EmdClientImplV1 emdClientDependencyV1() {return Mockito.mock(EmdClientImplV1.class);}

        @Bean
        public AccessTokenExpiringMap accessTokenExpiringMapDependency() {
            return Mockito.mock(AccessTokenExpiringMap.class);
        }

        @Bean
        public PnEmdIntegrationConfigs pnEmdIntegrationConfigsDependency() {
            return Mockito.mock(PnEmdIntegrationConfigs.class);
        }

        @Bean
        public ReactiveRedisService<RetrievalPayload> redisServiceDependency() {
            return Mockito.mock(ReactiveRedisService.class);
        }
    }
}
