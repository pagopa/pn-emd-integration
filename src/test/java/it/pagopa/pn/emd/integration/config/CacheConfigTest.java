package it.pagopa.pn.emd.integration.config;

import it.pagopa.pn.emd.integration.cache.IamRedisCredentialsProviderFactory;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheConfigTest {

    private PnEmdIntegrationConfigs.CacheConfigs cacheConfigs;
    private CacheConfig cacheConfig;

    @BeforeEach
    void setUp() {
        cacheConfigs = mock(PnEmdIntegrationConfigs.CacheConfigs.class);
        when(cacheConfigs.getHostName()).thenReturn("localhost");
        when(cacheConfigs.getPort()).thenReturn(6379);

        PnEmdIntegrationConfigs configs = mock(PnEmdIntegrationConfigs.class);
        when(configs.getRedisCache()).thenReturn(cacheConfigs);

        cacheConfig = new CacheConfig(configs);
    }

    // -------------------------------------------------------------------------
    // localConnectionFactory
    // -------------------------------------------------------------------------

    @Test
    void localConnectionFactory_createsLettuceConnectionFactory() {
        LettuceConnectionFactory factory = cacheConfig.localConnectionFactory();
        assertNotNull(factory);
    }

    @Test
    void localConnectionFactory_usesConfiguredHostAndPort() {
        when(cacheConfigs.getHostName()).thenReturn("my-local-redis");
        when(cacheConfigs.getPort()).thenReturn(6380);

        LettuceConnectionFactory factory = cacheConfig.localConnectionFactory();

        assertEquals("my-local-redis", factory.getStandaloneConfiguration().getHostName());
        assertEquals(6380, factory.getStandaloneConfiguration().getPort());
    }

    @Test
    void localConnectionFactory_hasSslDisabled() {
        LettuceConnectionFactory factory = cacheConfig.localConnectionFactory();
        assertFalse(factory.getClientConfiguration().isUseSsl());
    }

    @Test
    void localConnectionFactory_hasNoIamCredentialsProviderFactory() {
        LettuceConnectionFactory factory = cacheConfig.localConnectionFactory();
        assertTrue(factory.getClientConfiguration().getRedisCredentialsProviderFactory().isEmpty());
    }

    // -------------------------------------------------------------------------
    // elasticacheConnectionFactory
    // -------------------------------------------------------------------------

    @Test
    void elasticacheConnectionFactory_createsLettuceConnectionFactory() {
        LettuceConnectionFactory factory = cacheConfig.elasticacheConnectionFactory();
        assertNotNull(factory);
    }

    @Test
    void elasticacheConnectionFactory_hasSslEnabled() {
        LettuceConnectionFactory factory = cacheConfig.elasticacheConnectionFactory();
        assertTrue(factory.getClientConfiguration().isUseSsl());
    }

    @Test
    void elasticacheConnectionFactory_usesConfiguredHostAndPort() {
        when(cacheConfigs.getHostName()).thenReturn("my-elasticache-host");
        when(cacheConfigs.getPort()).thenReturn(6380);

        LettuceConnectionFactory factory = cacheConfig.elasticacheConnectionFactory();

        assertEquals("my-elasticache-host", factory.getStandaloneConfiguration().getHostName());
        assertEquals(6380, factory.getStandaloneConfiguration().getPort());
    }

    @Test
    void elasticacheConnectionFactory_hasIamCredentialsProviderFactory() {
        LettuceConnectionFactory factory = cacheConfig.elasticacheConnectionFactory();

        assertTrue(factory.getClientConfiguration().getRedisCredentialsProviderFactory().isPresent());
        assertInstanceOf(IamRedisCredentialsProviderFactory.class,
                factory.getClientConfiguration().getRedisCredentialsProviderFactory().get());
    }

    // -------------------------------------------------------------------------
    // reactiveRetrievalPayloadRedisTemplate
    // -------------------------------------------------------------------------

    @Test
    void reactiveRetrievalPayloadRedisTemplate_createsTemplate() {
        ReactiveRedisConnectionFactory factory = mock(ReactiveRedisConnectionFactory.class);
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(factory);
        assertNotNull(template);
    }

    @Test
    void reactiveTemplate_keySerializerIsString() {
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(mock(ReactiveRedisConnectionFactory.class));
        RedisSerializationContext<String, RetrievalPayload> ctx = template.getSerializationContext();

        ByteBuffer buffer = ctx.getKeySerializationPair().write("test-key");
        assertEquals("test-key", readString(buffer));
    }

    @Test
    void reactiveTemplate_hashKeySerializerIsString() {
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(mock(ReactiveRedisConnectionFactory.class));
        RedisSerializationContext<String, RetrievalPayload> ctx = template.getSerializationContext();

        ByteBuffer buffer = ctx.getHashKeySerializationPair().write("hash-key");
        assertEquals("hash-key", readString(buffer));
    }

    @Test
    void reactiveTemplate_valueSerializerProducesJson() {
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(mock(ReactiveRedisConnectionFactory.class));
        RedisSerializationContext<String, RetrievalPayload> ctx = template.getSerializationContext();

        RetrievalPayload payload = new RetrievalPayload("test-retrieval-id");
        String json = readString(ctx.getValueSerializationPair().write(payload));

        assertTrue(json.contains("retrievalId"));
        assertTrue(json.contains("test-retrieval-id"));
    }

    @Test
    void reactiveTemplate_hashValueSerializerProducesJson() {
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(mock(ReactiveRedisConnectionFactory.class));
        RedisSerializationContext<String, RetrievalPayload> ctx = template.getSerializationContext();

        RetrievalPayload payload = new RetrievalPayload("test-retrieval-id");
        String json = readString(ctx.getHashValueSerializationPair().write(payload));

        assertTrue(json.contains("retrievalId"));
        assertTrue(json.contains("test-retrieval-id"));
    }

    @Test
    void reactiveTemplate_objectMapperHasDefaultTypingEnabled() {
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(mock(ReactiveRedisConnectionFactory.class));
        RedisSerializationContext<String, RetrievalPayload> ctx = template.getSerializationContext();

        RetrievalPayload payload = new RetrievalPayload("test-retrieval-id");
        String json = readString(ctx.getValueSerializationPair().write(payload));

        assertTrue(json.contains("@class"),
                "JSON should contain @class property — ObjectMapper default typing is not active");
    }

    @Test
    void reactiveTemplate_serializationRoundTrip() {
        ReactiveRedisTemplate<String, RetrievalPayload> template =
                cacheConfig.reactiveRetrievalPayloadRedisTemplate(mock(ReactiveRedisConnectionFactory.class));
        RedisSerializationContext<String, RetrievalPayload> ctx = template.getSerializationContext();

        RetrievalPayload original = new RetrievalPayload("test-retrieval-id");
        original.setTppId("tpp-id-test-001");
        original.setDeeplink("https://example.com/deeplink/test");
        original.setIsPaymentEnabled(true);

        ByteBuffer buffer = ctx.getValueSerializationPair().write(original);
        RetrievalPayload deserialized = ctx.getValueSerializationPair().read(buffer);

        assertEquals(original, deserialized);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private String readString(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
