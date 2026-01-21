package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.cache.AccessTokenExpiringMap;
import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emd.integration.middleware.client.EmdClientImpl;
import it.pagopa.pn.emd.integration.utils.PnEmdIntegrationCostants;
import it.pagopa.pn.emd.integration.utils.Utils;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@ConditionalOnProperty(
        name = "pn.emd-integration.message.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmdMessageServiceImpl implements EmdMessageService {
    private final EmdClientImpl emdClient;
    private final AccessTokenExpiringMap accessTokenExpiringMap;
    private final PnEmdIntegrationConfigs pnEmdIntegrationConfigs;

    @Override
    public Mono<InlineResponse200> submitMessage(SendMessageRequestBody request) {
        log.info("Start submitMessage for request: {}", request);
        return accessTokenExpiringMap.getAccessToken().flatMap(token -> {
            String reqId = UUID.randomUUID().toString();
            return emdClient.submitMessage(sendMessageRequestMap(request), token.getAccessToken(), reqId);
        });
    }

    private SendMessageRequest sendMessageRequestMap(SendMessageRequestBody request) {
        boolean isDigital = request.getDeliveryMode().equals(SendMessageRequestBody.DeliveryModeEnum.DIGITAL);
        PnEmdIntegrationConfigs.CourtesyMessageTemplate messageTemplate = createMessages(request, isDigital);

        log.info("created new Message with deliveryMode: '{}', Header File: '{}', Content File: '{}'",
                request.getDeliveryMode(), messageTemplate.getHeaderFileName(), messageTemplate.getContentFileName());

        String messageContent = isDigital
                ? messageTemplate.getContent()
                : buildAnalogContent(messageTemplate.getContent(), request.getSchedulingAnalogDate().toInstant());

        SendMessageRequest.WorkflowTypeEnum workflowType = isDigital
                ? SendMessageRequest.WorkflowTypeEnum.DIGITAL
                : SendMessageRequest.WorkflowTypeEnum.ANALOG;

        SendMessageRequest.SendMessageRequestBuilder builder = SendMessageRequest.builder()
                .messageId(request.getOriginId() + "_" + Utils.removePrefix(request.getInternalRecipientId()))
                .recipientId(request.getRecipientId())
                .triggerDateTime(Instant.now())
                .senderDescription(request.getSenderDescription())
                .associatedPayment(request.getAssociatedPayment())
                .messageUrl(URI.create(pnEmdIntegrationConfigs.getOriginalMessageUrl()))
                .originId(request.getOriginId())
                .channel(SendMessageRequest.ChannelEnum.SEND)
                .title(messageTemplate.getHeader())
                .content(messageContent)
                .workflowType(workflowType);

        if (!isDigital) {
            builder.analogSchedulingDate(request.getSchedulingAnalogDate().toInstant());
        }

        return builder.build();
    }

    private PnEmdIntegrationConfigs.CourtesyMessageTemplate getTemplate(boolean isDigital) {
        return isDigital
                ? pnEmdIntegrationConfigs.getMsgsTemplates().getDigitalMsg()
                : pnEmdIntegrationConfigs.getMsgsTemplates().getAnalogMsg();
    }

    private PnEmdIntegrationConfigs.CourtesyMessageTemplate createMessages(SendMessageRequestBody request, boolean isDigital) {
        if (!isDigital && request.getSchedulingAnalogDate() == null) {
            throw new PnEmdIntegrationException(
                    "Missing schedulingAnalogDate for analog delivery mode",
                    "Field 'schedulingAnalogDate' must be provided",
                    PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_INVALID_REQUEST_MISSING_SCHEDULING_ANALOG_DATE,
                    "schedulingAnalogDate"
            );
        }

        return getTemplate(isDigital);
    }

    private String buildAnalogContent(String template, Instant schedulingAnalogDate) {
        log.debug("Building analog content with schedulingAnalogDate: {}", schedulingAnalogDate);
        String localDateTimeItaly = LocalDateTime.ofInstant(schedulingAnalogDate, ZoneId.of("Europe/Rome")).format(PnEmdIntegrationCostants.PROBABLE_SCHEDULING_ANALOG_DATE_DATE_FORMATTER);
        String[] schedulingDateWithHourItaly = localDateTimeItaly.split(" ");

        return template
                .replace(PnEmdIntegrationCostants.DATE_PLACEHOLDER, schedulingDateWithHourItaly[0])
                .replace(PnEmdIntegrationCostants.TIME_PLACEHOLDER, schedulingDateWithHourItaly[1]);
    }
}
