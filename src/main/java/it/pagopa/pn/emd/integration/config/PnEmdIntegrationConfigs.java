package it.pagopa.pn.emd.integration.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.emd.integration.cache.RedisMode;
import lombok.CustomLog;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.time.Duration;

import static it.pagopa.pn.emd.integration.utils.Utils.fetchTemplate;

@Configuration
@ConfigurationProperties( prefix = "pn.emd-integration")
@CustomLog
@Data
@Import({SharedAutoConfiguration.class})
public class PnEmdIntegrationConfigs {
    private String milClientSecret;
    private String milClientId;
    private String milBasePath;
    private String emdCoreBasePath;
    private String originalMessageUrl;
    private String emdPaymentEndpoint;
    // Token expiration buffer in milliseconds
    private long milTokenExpirationBuffer;

    private CacheConfigs redisCache;
    private Duration retrievalPayloadCacheTtl;

    private String courtesyMessageContent;

    private MessagesTemplate msgsTemplate;

    @Data
    public static class CacheConfigs {
        private String hostName;
        private int port;
        private String userId;
        private String cacheName;
        private String cacheRegion;
        private RedisMode mode;
    }

    @Data
    public static class MessagesTemplate {
        private String contentAnalogMsg;
        private String contentDigitalMsg;
        private String headerAnalogMsg;
        private String headerDigitalMsg;
    }

    @PostConstruct
    public void init() {
        this.msgsTemplate = new MessagesTemplate();
        this.msgsTemplate.contentAnalogMsg = fetchTemplate("message_templates/content_analog_message.md");
        this.msgsTemplate.contentDigitalMsg = fetchTemplate("message_templates/content_digital_message.md");
        this.msgsTemplate.headerAnalogMsg = fetchTemplate("message_templates/header_analog_message.md");
        this.msgsTemplate.headerDigitalMsg = fetchTemplate("message_templates/header_digital_message.md");
        log.info("Messages templates loaded successfully.");
    }

}