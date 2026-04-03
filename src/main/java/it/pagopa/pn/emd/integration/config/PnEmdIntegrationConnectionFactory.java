package it.pagopa.pn.emd.integration.config;
import it.pagopa.pn.emd.integration.cache.IAMAuthTokenRequest;
import it.pagopa.pn.emd.integration.cache.RedisMode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.net.URISyntaxException;
/**
 * A custom connection factory extending the JedisConnectionFactory. This connection factory implements an IAM based authentication for Redis and a scheduled IAM token refresh.
 */
@Slf4j
public class PnEmdIntegrationConnectionFactory extends JedisConnectionFactory {

    // Token refresh interval is set to 14 minutes because the token expires after 15 minutes. Reference: https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/auth-iam.html#:~:text=The%20IAM%20authentication%20token%20is%20valid%20for%2015%20minutes.%20For%20long%2Dlived%20connections%2C%20we%20recommend%20using%20a%20Redis%20OSS%20client%20that%20supports%20a%20credentials%20provider%20interface.
    private static final Long TOKEN_REFRESH_MINUTES = 14L;
    private final IAMAuthTokenRequest iamAuthTokenRequest;
    @Getter
    private final RedisMode redisMode;
    @Getter
    private final String userId;
    @Getter
    private final String cacheName;
    @Getter
    private final String region;

    /**
     * Instantiates a new Pn log extractor connection factory.
     *
     * @param standaloneConfiguration  the standalone configuration
     * @param cacheConfigs             the cache configs
     * @throws URISyntaxException the uri syntax exception
     */
    public PnEmdIntegrationConnectionFactory(
            RedisStandaloneConfiguration standaloneConfiguration,
            JedisClientConfiguration jedisClientConfiguration,
            PnEmdIntegrationConfigs.CacheConfigs cacheConfigs
    ) throws URISyntaxException {
        super(standaloneConfiguration, jedisClientConfiguration);
        this.redisMode = cacheConfigs.getMode();
        this.userId = cacheConfigs.getUserId();
        this.cacheName = cacheConfigs.getCacheName();
        this.region = cacheConfigs.getCacheRegion();
        this.iamAuthTokenRequest = new IAMAuthTokenRequest(this.userId, this.cacheName, this.region, redisMode == RedisMode.SERVERLESS);
    }

}
