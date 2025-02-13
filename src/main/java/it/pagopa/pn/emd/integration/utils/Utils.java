package it.pagopa.pn.emd.integration.utils;

public class Utils {
    private Utils() {}
    public static String removePrefix(String input) {
        if (input != null && (input.startsWith("PG-") || input.startsWith("PF-"))) {
            return input.substring(3);
        }
        return input;
    }
}
