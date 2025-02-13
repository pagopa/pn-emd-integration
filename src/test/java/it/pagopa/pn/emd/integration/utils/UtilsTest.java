package it.pagopa.pn.emd.integration.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}