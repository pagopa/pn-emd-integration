package it.pagopa.pn.emd.integration.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class PnEmdIntegrationConfigsBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "pn.emd-integration.keycloak-client-id=test-client-id",
                    "pn.emd-integration.keycloak-client-secret=test-client-secret",
                    "pn.emd-integration.keycloak-token-endpoint=https://keycloak.example.com/token",
                    "pn.emd-integration.keycloak-token-expiration-buffer=2000"
            );

    @Test
    void keycloakClientId_bindsFromProperty() {
        contextRunner.run(context -> {
            PnEmdIntegrationConfigs configs = context.getBean(PnEmdIntegrationConfigs.class);
            assertThat(configs.getKeycloakClientId()).isEqualTo("test-client-id");
        });
    }

    @Test
    void keycloakClientSecret_bindsFromProperty() {
        contextRunner.run(context -> {
            PnEmdIntegrationConfigs configs = context.getBean(PnEmdIntegrationConfigs.class);
            assertThat(configs.getKeycloakClientSecret()).isEqualTo("test-client-secret");
        });
    }

    @Test
    void keycloakTokenEndpoint_bindsFromProperty() {
        contextRunner.run(context -> {
            PnEmdIntegrationConfigs configs = context.getBean(PnEmdIntegrationConfigs.class);
            assertThat(configs.getKeycloakTokenEndpoint()).isEqualTo("https://keycloak.example.com/token");
        });
    }

    @Test
    void keycloakTokenExpirationBuffer_bindsFromProperty() {
        contextRunner.run(context -> {
            PnEmdIntegrationConfigs configs = context.getBean(PnEmdIntegrationConfigs.class);
            assertThat(configs.getKeycloakTokenExpirationBuffer()).isEqualTo(2000L);
        });
    }

    @Configuration
    @EnableConfigurationProperties(PnEmdIntegrationConfigs.class)
    static class TestConfig {
    }
}
