package it.pagopa.pn.emd.integration.config;

import io.micrometer.core.instrument.util.IOUtils;
import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import it.pagopa.pn.emd.integration.cache.RedisMode;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_ERROR_CODE_BADCONFIGURATION_MISSING_TEMPLATE;

@Configuration
@ConfigurationProperties( prefix = "pn.emd-integration")
@Slf4j
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
        this.msgsTemplate.contentAnalogMsg = fetchMessage("content_analog_message.md");
        this.msgsTemplate.contentDigitalMsg = fetchMessage("content_digital_message.md");
        this.msgsTemplate.headerAnalogMsg = fetchMessage("header_analog_message.md");
        this.msgsTemplate.headerDigitalMsg = fetchMessage("header_digital_message.md");
    }

    private String fetchMessage(String filename) {
        try( InputStream in = getInputStreamFromResource(filename)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("cannot load message from resources", e);
            throw new PnEmdIntegrationException("cannot load template ", PN_EMD_INTEGRATION_ERROR_CODE_BADCONFIGURATION_MISSING_TEMPLATE);
        }
    }

    private InputStream getInputStreamFromResource(String filename) throws IOException {
        return ResourceUtils.getURL("classpath:message_templates/" + filename).openStream();
    }

}