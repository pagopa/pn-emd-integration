package it.pagopa.pn.emd.integration.utils;

public class Utils {

    public static String removePrefix(String input) {
        if (input != null) {
            if (input.startsWith("PG-")) {
                return input.substring(3);
            } else if (input.startsWith("PF-")) {
                return input.substring(3);
            }
        }
        return input;
    }
}
