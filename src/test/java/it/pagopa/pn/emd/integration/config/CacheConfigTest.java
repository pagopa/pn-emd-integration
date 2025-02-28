package it.pagopa.pn.emd.integration.config;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
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

        PnEmdIntegrationConfigs pnEmdIntegrationConfigs = mock(PnEmdIntegrationConfigs.class);
        when(pnEmdIntegrationConfigs.getRedisCache()).thenReturn(cacheConfigs);

        CacheConfig cacheConfig = new CacheConfig(pnEmdIntegrationConfigs);
        ReactiveRedisConnectionFactory factory = cacheConfig.localConnectionFactory();

        assertNotNull(factory);
        assert(factory instanceof LettuceConnectionFactory);
    }

    @Test
    void retrievalPayloadOps_createsReactiveRedisTemplate() {
        ReactiveRedisConnectionFactory factory = mock(ReactiveRedisConnectionFactory.class);
        CacheConfig cacheConfig = new CacheConfig(mock(PnEmdIntegrationConfigs.class));

        ReactiveRedisTemplate<String, RetrievalPayload> template = cacheConfig.retrievalPayloadOps(factory);

        assertNotNull(template);
        RedisSerializationContext<String, RetrievalPayload> serializationContext = template.getSerializationContext();
        assertNotNull(serializationContext);
    }
}