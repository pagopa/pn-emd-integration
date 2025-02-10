package it.pagopa.pn.emd.integration.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.emd.integration.cache.RedisMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;

@Configuration
@ConfigurationProperties( prefix = "pn.emd-integration")
@Data
@Import({SharedAutoConfiguration.class})
public class PnEmdIntegrationConfigs {
    private String milClientSecret;
    private String milClientId;
    private String milBasePath;
    // Token expiration buffer in milliseconds
    private long milTokenExpirationBuffer;

    private CacheConfigs redisCache;
    private Duration retrievalPayloadCacheExpiration;

    @Data
    public static class CacheConfigs {
        private String hostName;
        private int port;
        private String userId;
        private String cacheName;
        private String cacheRegion;
        private RedisMode mode;
    }
}
