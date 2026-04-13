package it.pagopa.pn.emd.integration.config.springbootcfg;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ActivatorTest {

    @Test
    void activatorTest() {
        Assertions.assertDoesNotThrow(() -> {
            // SpringAnalyzerActivation and SpringAnalyzerClientConfig have been removed
            // as part of pn-commons removal. This test is a placeholder.
        });
    }
}
