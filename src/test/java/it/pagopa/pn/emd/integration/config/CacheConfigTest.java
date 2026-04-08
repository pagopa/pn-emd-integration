package it.pagopa.pn.emd.integration.config;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("local")
class CacheConfigTest {

    @Test
    void localConnectionFactory_createsLettuceConnectionFactory() {
        PnEmdIntegrationConfigs.CacheConfigs cacheConfigs = mock(PnEmdIntegrationConfigs.CacheConfigs.class);
        when(cacheConfigs.getHostName()).thenReturn("localhost");
        when(cacheConfigs.getPort()).thenReturn(6379);

        PnEmdIntegrationConfigs configs = mock(PnEmdIntegrationConfigs.class);
        when(configs.getRedisCache()).thenReturn(cacheConfigs);

        CacheConfig cacheConfig = new CacheConfig(configs);
        LettuceConnectionFactory factory = cacheConfig.localConnectionFactory();

        assertNotNull(factory);
    }

    @Test
    void reactiveRetrievalPayloadRedisTemplate_createsTemplate() {
        ReactiveRedisConnectionFactory factory = mock(ReactiveRedisConnectionFactory.class);
        CacheConfig cacheConfig = new CacheConfig(mock(PnEmdIntegrationConfigs.class));

        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(factory);

        assertNotNull(template);
    }

}
