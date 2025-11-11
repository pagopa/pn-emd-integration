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

    private Templates msgsTemplates;

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
    public static class CourtesyMessageTemplate {
        private String fileNameHeader;
        private String fileNameContent;
        private String header;
        private String content;
    }


    @Data
    public static class Templates {

        private CourtesyMessageTemplate analogMsg;
        private CourtesyMessageTemplate digitalMsg;

        private String contentAnalogFile = "content_analog_message.md";
        private String contentDigitalFile = "content_digital_message.md";
        private String headerAnalogFile = "header_analog_message.md";
        private String headerDigitalFile = "header_digital_message.md";

    }

    @PostConstruct
    public void init() {
        this.msgsTemplates = new Templates();

        CourtesyMessageTemplate analog = new CourtesyMessageTemplate();
        CourtesyMessageTemplate digital = new CourtesyMessageTemplate();

        analog.setFileNameContent(this.msgsTemplates.getContentAnalogFile());
        analog.setFileNameHeader(this.msgsTemplates.getHeaderAnalogFile());
        analog.setContent(fetchTemplate("message_templates/" + analog.getFileNameContent()));
        analog.setHeader(fetchTemplate("message_templates/" + analog.getFileNameHeader()));

        digital.setFileNameContent(this.msgsTemplates.getContentDigitalFile());
        digital.setFileNameHeader(this.msgsTemplates.getHeaderDigitalFile());
        digital.setContent(fetchTemplate("message_templates/" + digital.getFileNameContent()));
        digital.setHeader(fetchTemplate("message_templates/" + digital.getFileNameHeader()));


        this.msgsTemplates.setAnalogMsg(analog);
        this.msgsTemplates.setDigitalMsg(digital);

        log.info("Messages templates loaded successfully.");

    }




}