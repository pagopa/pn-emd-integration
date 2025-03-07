package it.pagopa.pn.emd.integration.config;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("local")
class CacheConfigTest {

    @Test
    void localConnectionFactory_createsJedisConnectionFactory() {
        PnEmdIntegrationConfigs.CacheConfigs cacheConfigs = mock(PnEmdIntegrationConfigs.CacheConfigs.class);
        when(cacheConfigs.getHostName()).thenReturn("localhost");
        when(cacheConfigs.getPort()).thenReturn(6379);

        PnEmdIntegrationConfigs pnEmdIntegrationConfigs = mock(PnEmdIntegrationConfigs.class);
        when(pnEmdIntegrationConfigs.getRedisCache()).thenReturn(cacheConfigs);

        CacheConfig cacheConfig = new CacheConfig(pnEmdIntegrationConfigs);
        JedisConnectionFactory factory = cacheConfig.localConnectionFactory();

        assertNotNull(factory);
    }

    @Test
    void retrievalPayloadOps_createsRedisTemplate() {
        JedisConnectionFactory factory = mock(JedisConnectionFactory.class);
        CacheConfig cacheConfig = new CacheConfig(mock(PnEmdIntegrationConfigs.class));

        RedisTemplate<String, RetrievalPayload> template = cacheConfig.retrievalPayloadRedisTemplate(factory);

        assertNotNull(template);
    }
}