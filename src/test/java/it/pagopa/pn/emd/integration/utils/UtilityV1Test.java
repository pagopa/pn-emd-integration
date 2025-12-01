package it.pagopa.pn.emd.integration.utils;

import it.pagopa.pn.emd.integration.config.PnEmdIntegrationConfigs;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v1.model.SendMessageRequest;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilityV1Test {

    private PnEmdIntegrationConfigs pnConfigs;
    private PnEmdIntegrationConfigs.Templates templates;
    private PnEmdIntegrationConfigs.CourtesyMessageTemplate digitalTemplate;
    private PnEmdIntegrationConfigs.CourtesyMessageTemplate analogTemplate;

    @BeforeEach
    void setup() {
        pnConfigs = mock(PnEmdIntegrationConfigs.class);
        templates = mock(PnEmdIntegrationConfigs.Templates.class);
        digitalTemplate = mock(PnEmdIntegrationConfigs.CourtesyMessageTemplate.class);
        analogTemplate = mock(PnEmdIntegrationConfigs.CourtesyMessageTemplate.class);

        when(pnConfigs.getMsgsTemplates()).thenReturn(templates);
        when(templates.getDigitalMsg()).thenReturn(digitalTemplate);
        when(templates.getAnalogMsg()).thenReturn(analogTemplate);

        when(digitalTemplate.getHeader()).thenReturn("DIGITAL_HEADER");
        when(analogTemplate.getHeader()).thenReturn("ANALOG_HEADER");

        when(pnConfigs.getOriginalMessageUrl()).thenReturn("https://test.it/message");
    }

    private SendMessageRequestBody baseRequest() {
        SendMessageRequestBody req = new SendMessageRequestBody();
        req.setOriginId("ORIG123");
        req.setInternalRecipientId("prefix-RECIPIENT");
        req.setRecipientId("RECIPIENT");
        req.setSenderDescription("Sender DESC");
        return req;
    }

    @Test
    void testSendMessageRequestMapV1_digital_ok() {
        SendMessageRequestBody req = baseRequest();
        req.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.DIGITAL);

        SendMessageRequest result = UtilityV1.sendMessageRequestMapV1(req, pnConfigs);

        assertNotNull(result);

        assertEquals("DIGITAL_HEADER", result.getContent());
        assertEquals(SendMessageRequest.ChannelEnum.SEND, result.getChannel());
        assertNotNull(result.getTriggerDateTime());

        verify(templates).getDigitalMsg();
        verify(templates, never()).getAnalogMsg();
    }

    @Test
    void testSendMessageRequestMapV1_analog_ok() {
        SendMessageRequestBody req = baseRequest();
        req.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.ANALOG);
        req.setSchedulingAnalogDate(new Date());

        SendMessageRequest result = UtilityV1.sendMessageRequestMapV1(req, pnConfigs);

        assertNotNull(result);
        assertEquals("ANALOG_HEADER", result.getContent());

        verify(templates).getAnalogMsg();
        verify(templates, never()).getDigitalMsg();
    }

    @Test
    void testSendMessageRequestMapV1_analog_missingScheduling_throwsException() {
        SendMessageRequestBody req = baseRequest();
        req.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.ANALOG);
        req.setSchedulingAnalogDate(null);

        PnEmdIntegrationException ex = assertThrows(
                PnEmdIntegrationException.class,
                () -> UtilityV1.sendMessageRequestMapV1(req, pnConfigs)
                                                   );

        assertTrue(ex.getMessage().contains("Missing schedulingAnalogDate"));
    }
}
