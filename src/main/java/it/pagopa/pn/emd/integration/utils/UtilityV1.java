package it.pagopa.pn.emd.integration.utils;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import lombok.CustomLog;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@CustomLog
public class UtilityV1 {

    private UtilityV1() {
    }

    public static SendMessageRequest sendMessageRequestMapV1(
            SendMessageRequestBody request,
            PnEmdIntegrationConfigs pnEmdIntegrationConfigs) {

        boolean isDigital = request.getDeliveryMode().equals(SendMessageRequestBody.DeliveryModeEnum.DIGITAL);
        PnEmdIntegrationConfigs.CourtesyMessageTemplate messageTemplate =
                createMessages(request, isDigital, pnEmdIntegrationConfigs);

        String messageContent = isDigital ? messageTemplate.getContent() :
                buildAnalogContent(messageTemplate.getContent(), request.getSchedulingAnalogDate().toInstant());

        return SendMessageRequest.builder()
                                 .messageId(request.getOriginId() + "_" + Utils.removePrefix(request.getInternalRecipientId()))
                                 .recipientId(request.getRecipientId())
                                 .content(messageTemplate.getHeader())
                                 .notes(messageContent)
                                 .triggerDateTime(Instant.now())
                                 .senderDescription(request.getSenderDescription())
                                 .associatedPayment(request.getAssociatedPayment())
                                 .messageUrl(URI.create(pnEmdIntegrationConfigs.getOriginalMessageUrl()))
                                 .originId(request.getOriginId())
                                 .channel(SendMessageRequest.ChannelEnum.SEND)
                                 .build();
    }

    private static PnEmdIntegrationConfigs.CourtesyMessageTemplate createMessages(
            SendMessageRequestBody request,
            boolean isDigital,
            PnEmdIntegrationConfigs pnEmdIntegrationConfigs) {

        if (!isDigital && request.getSchedulingAnalogDate() == null) {
            throw new PnEmdIntegrationException(
                    "Missing schedulingAnalogDate for analog delivery mode",
                    "Field 'schedulingAnalogDate' must be provided",
                    PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_INVALID_REQUEST_MISSING_SCHEDULING_ANALOG_DATE,
                    "schedulingAnalogDate"
            );
        }

        return getTemplate(isDigital, pnEmdIntegrationConfigs);
    }

    private static String buildAnalogContent(String template, Instant schedulingAnalogDate) {
        log.debug("Building analog content with schedulingAnalogDate: {}", schedulingAnalogDate);
        String localDateTimeItaly = LocalDateTime.ofInstant(schedulingAnalogDate, ZoneId.of("Europe/Rome"))
                                                 .format(PnEmdIntegrationCostants.PROBABLE_SCHEDULING_ANALOG_DATE_DATE_FORMATTER);

        String[] schedulingDateWithHourItaly = localDateTimeItaly.split(" ");
        return template.replace(PnEmdIntegrationCostants.DATE_PLACEHOLDER, schedulingDateWithHourItaly[0])
                       .replace(PnEmdIntegrationCostants.TIME_PLACEHOLDER, schedulingDateWithHourItaly[1]);
    }

    private static PnEmdIntegrationConfigs.CourtesyMessageTemplate getTemplate(
            boolean isDigital,
            PnEmdIntegrationConfigs pnEmdIntegrationConfigs) {

        return isDigital
                ? pnEmdIntegrationConfigs.getMsgsTemplates().getDigitalMsg()
                : pnEmdIntegrationConfigs.getMsgsTemplates().getAnalogMsg();
    }

}