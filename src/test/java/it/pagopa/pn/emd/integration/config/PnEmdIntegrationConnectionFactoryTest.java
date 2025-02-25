package it.pagopa.pn.emd.integration.config;

    import it.pagopa.pn.emd.integration.cache.RedisMode;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.mockito.Mock;
    import org.mockito.MockitoAnnotations;
    import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
    import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

    import java.net.URISyntaxException;

    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.mockito.Mockito.when;

    class PnEmdIntegrationConnectionFactoryTest {

        @Mock
        private PnEmdIntegrationConfigs.CacheConfigs cacheConfigs;

        @Mock
        private RedisStandaloneConfiguration standaloneConfiguration;

        @Mock
        private LettuceClientConfiguration lettuceClientConfiguration;

        private PnEmdIntegrationConnectionFactory connectionFactory;

        @BeforeEach
        void setUp() throws URISyntaxException {
            MockitoAnnotations.openMocks(this);
            when(cacheConfigs.getMode()).thenReturn(RedisMode.SERVERLESS);
            when(cacheConfigs.getUserId()).thenReturn("testUserId");
            when(cacheConfigs.getCacheName()).thenReturn("testCacheName");
            when(cacheConfigs.getCacheRegion()).thenReturn("testRegion");

            connectionFactory = new PnEmdIntegrationConnectionFactory(standaloneConfiguration, lettuceClientConfiguration, cacheConfigs);
        }

        @Test
        void getRedisMode() {
            assertEquals(RedisMode.SERVERLESS, connectionFactory.getRedisMode());
        }

        @Test
        void getUserId() {
            assertEquals("testUserId", connectionFactory.getUserId());
        }

        @Test
        void getCacheName() {
            assertEquals("testCacheName", connectionFactory.getCacheName());
        }

        @Test
        void getRegion() {
            assertEquals("testRegion", connectionFactory.getRegion());
        }
    }