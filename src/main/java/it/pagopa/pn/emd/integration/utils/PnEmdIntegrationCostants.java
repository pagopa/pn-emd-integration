package it.pagopa.pn.emd.integration.utils;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PnEmdIntegrationCostants {
    private PnEmdIntegrationCostants() {
    }

    public static final DateTimeFormatter PROBABLE_SCHEDULING_ANALOG_DATE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALIAN);
    public static final String DATE_PLACEHOLDER = "{{schedulingAnalogDate}}";
    public static final String TIME_PLACEHOLDER = "{{schedulingAnalogTime}}";
    public static final String MESSAGE_TEMPLATES_BASE_PATH = "message_templates/";
}