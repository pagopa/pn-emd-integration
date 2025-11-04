package it.pagopa.pn.emd.integration.utils;

import io.micrometer.core.instrument.util.IOUtils;
import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import lombok.CustomLog;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_ERROR_CODE_BAD_CONFIGURATION_MISSING_TEMPLATE;

@CustomLog
public class Utils {
    private Utils() {}
    public static String removePrefix(String input) {
        if (input != null && (input.startsWith("PG-") || input.startsWith("PF-"))) {
            return input.substring(3);
        }
        return input;
    }

    /**
     * Loads the content of a template file from the classpath resources.
     * <p>
     * The file is read as a UTF-8 string. If an error occurs during loading,
     * the error is logged and a {@link it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException} is thrown.
     * </p>
     *
     * @param filePath the relative path of the template file in the resources folder (e.g., "message_templates/content_analog_message.md")
     * @return the content of the file as a string
     * @throws it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException if the file cannot be loaded
     */
    public static String fetchTemplate(String filePath) {
        try (InputStream in = getInputStreamFromResource(filePath)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("cannot load message from resources", e);
            throw new PnEmdIntegrationException("cannot load template ", PN_EMD_INTEGRATION_ERROR_CODE_BAD_CONFIGURATION_MISSING_TEMPLATE);
        }
    }

    private static InputStream getInputStreamFromResource(String filePath) throws IOException {
        return ResourceUtils.getURL("classpath:" + filePath).openStream();
    }
}
