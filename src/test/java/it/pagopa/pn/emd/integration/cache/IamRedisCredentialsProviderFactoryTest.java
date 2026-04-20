package it.pagopa.pn.emd.integration.cache;

import io.lettuce.core.RedisCredentialsProvider;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamRedisCredentialsProviderFactoryTest {

    @BeforeAll
    static void setUpAwsCredentials() {
        System.setProperty("aws.accessKeyId", "testAccessKey");
        System.setProperty("aws.secretAccessKey", "testSecretKey");
    }

    @AfterAll
    static void cleanUpAwsCredentials() {
        System.clearProperty("aws.accessKeyId");
        System.clearProperty("aws.secretAccessKey");
    }

    private PnEmdIntegrationConfigs.CacheConfigs mockCacheConfigs(RedisMode mode) {
        PnEmdIntegrationConfigs.CacheConfigs cacheConfigs = mock(PnEmdIntegrationConfigs.CacheConfigs.class);
        when(cacheConfigs.getUserId()).thenReturn("userId");
        when(cacheConfigs.getCacheName()).thenReturn("cacheName");
        when(cacheConfigs.getCacheRegion()).thenReturn("eu-south-1");
        when(cacheConfigs.getMode()).thenReturn(mode);
        return cacheConfigs;
    }

    @Test
    void createCredentialsProvider_serverlessMode_returnsNonNullProvider() {
        IamRedisCredentialsProviderFactory factory = new IamRedisCredentialsProviderFactory(mockCacheConfigs(RedisMode.SERVERLESS));
        RedisCredentialsProvider credentialsProvider = factory.createCredentialsProvider(new RedisStandaloneConfiguration());
        assertNotNull(credentialsProvider);
    }

    @Test
    void createCredentialsProvider_managedMode_returnsNonNullProvider() {
        IamRedisCredentialsProviderFactory factory = new IamRedisCredentialsProviderFactory(mockCacheConfigs(RedisMode.MANAGED));
        RedisCredentialsProvider credentialsProvider = factory.createCredentialsProvider(new RedisStandaloneConfiguration());
        assertNotNull(credentialsProvider);
    }

    @Test
    void createCredentialsProvider_serverlessMode_tokenContainsServerlessParameter() {
        IamRedisCredentialsProviderFactory factory = new IamRedisCredentialsProviderFactory(mockCacheConfigs(RedisMode.SERVERLESS));
        RedisCredentialsProvider credentialsProvider = factory.createCredentialsProvider(new RedisStandaloneConfiguration());

        StepVerifier.create(credentialsProvider.resolveCredentials())
                .assertNext(credentials -> {
                    assertEquals("userId", credentials.getUsername());
                    String token = new String(credentials.getPassword());
                    assertTrue(token.contains("ResourceType=ServerlessCache"),
                            "Serverless token should contain ResourceType=ServerlessCache");
                })
                .verifyComplete();
    }

    @Test
    void createCredentialsProvider_managedMode_tokenDoesNotContainServerlessParameter() {
        IamRedisCredentialsProviderFactory factory = new IamRedisCredentialsProviderFactory(mockCacheConfigs(RedisMode.MANAGED));
        RedisCredentialsProvider credentialsProvider = factory.createCredentialsProvider(new RedisStandaloneConfiguration());

        StepVerifier.create(credentialsProvider.resolveCredentials())
                .assertNext(credentials -> {
                    assertEquals("userId", credentials.getUsername());
                    String token = new String(credentials.getPassword());
                    assertFalse(token.contains("ResourceType=ServerlessCache"),
                            "Managed token should not contain ResourceType=ServerlessCache");
                })
                .verifyComplete();
    }
}
