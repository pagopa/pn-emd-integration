package it.pagopa.pn.emd.integration.utils;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_ERROR_CODE_BADCONFIGURATION_MISSING_TEMPLATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UtilsTest {

    @ParameterizedTest
    @CsvSource({
            "PG-12345, 12345",
            "PF-12345, 12345",
            "12345, 12345",
            "'', ''",
            "null, null"
    })
    void testRemovePrefix(String input, String expected) {
        String actual = Utils.removePrefix(input);
        assertEquals(expected, actual);
    }

    @Test
    void fetchTemplate_TestOk() {
        PnEmdIntegrationException ex = Assertions.assertThrows(PnEmdIntegrationException.class, () -> Utils.fetchTemplate("NON_EXISTENT_TEMPLATE"));
        assertEquals(PN_EMD_INTEGRATION_ERROR_CODE_BADCONFIGURATION_MISSING_TEMPLATE, ex.getCode());
    }

    @Test
    void fetchTemplate_MissingTemplate() {
        String template = Assertions.assertDoesNotThrow(() -> Utils.fetchTemplate("message_templates/test-template.md"));
        assertNotNull(template);
    }
}