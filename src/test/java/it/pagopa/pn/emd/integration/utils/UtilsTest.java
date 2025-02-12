package it.pagopa.pn.emd.integration.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilsTest {

    @Test
    public void testRemovePrefixWithPG() {
        String input = "PG-12345";
        String expected = "12345";
        String actual = Utils.removePrefix(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testRemovePrefixWithPF() {
        String input = "PF-12345";
        String expected = "12345";
        String actual = Utils.removePrefix(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testRemovePrefixWithoutPrefix() {
        String input = "12345";
        String expected = "12345";
        String actual = Utils.removePrefix(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testRemovePrefixWithNull() {
        String input = null;
        String expected = null;
        String actual = Utils.removePrefix(input);
        assertEquals(expected, actual);
    }

    @Test
    public void testRemovePrefixWithEmptyString() {
        String input = "";
        String expected = "";
        String actual = Utils.removePrefix(input);
        assertEquals(expected, actual);
    }
}