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
import static it.pagopa.pn.emd.integration.utils.PnEmdIntegrationCostants.MESSAGE_TEMPLATES_BASE_PATH;
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

    private Boolean enabledApiV2;

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
        private String header;
        private String headerFileName;
        private String content;
        private String contentFileName;
    }


    @Data
    public static class Templates {

        private CourtesyMessageTemplate analogMsg;
        private CourtesyMessageTemplate digitalMsg;

    }

    @PostConstruct
    public void init() {
        this.msgsTemplates = new Templates();

        CourtesyMessageTemplate analog = new CourtesyMessageTemplate();
        CourtesyMessageTemplate digital = new CourtesyMessageTemplate();

        //initialize analog message template
        analog.setHeaderFileName("header_analog_message.md");
        analog.setContentFileName("content_analog_message.md");
        analog.setHeader(fetchTemplate(MESSAGE_TEMPLATES_BASE_PATH + analog.getHeaderFileName()));
        analog.setContent(fetchTemplate(MESSAGE_TEMPLATES_BASE_PATH + analog.getContentFileName()));

        //initialize digital message template
        digital.setHeaderFileName("header_digital_message.md");
        digital.setContentFileName("content_digital_message.md");
        digital.setContent(fetchTemplate(MESSAGE_TEMPLATES_BASE_PATH + digital.getContentFileName()));
        digital.setHeader(fetchTemplate(MESSAGE_TEMPLATES_BASE_PATH + digital.getContentFileName()));

        //assign templates to msgsTemplates
        this.msgsTemplates.setAnalogMsg(analog);
        this.msgsTemplates.setDigitalMsg(digital);

        log.info("Messages templates loaded successfully.");

    }

}