package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
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
            .withUserConfiguration(TestConfig.class)
            .withUserConfiguration(EmdCoreServiceImpl.class, EmdCoreServiceDisabled.class);

    @Test
    void emdCoreServiceImplIsChosen() {
        contextRunner
                .withPropertyValues("pn.emd-integration.enabled=true")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdCoreServiceImpl.class),
                        () -> assertThat(context).doesNotHaveBean(EmdCoreServiceDisabled.class)));
    }

    @Test
    void emdCoreServiceDisabledIsChosen() {
        contextRunner
                .withPropertyValues("pn.emd-integration.enabled=false")
                .run(context -> assertAll(
                        () -> assertThat(context).hasSingleBean(EmdCoreServiceDisabled.class),
                        () -> assertThat(context).doesNotHaveBean(EmdCoreServiceImpl.class)));
    }


    @Configuration
    protected static class TestConfig {
        @Bean
        public EmdClientImpl emdClientDependency() {
            return Mockito.mock(EmdClientImpl.class);
        }

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