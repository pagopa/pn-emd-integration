package it.pagopa.pn.emd.integration.config.springbootcfg;

import io.micrometer.core.instrument.MeterRegistry;
import it.pagopa.pn.commons.utils.metrics.cloudwatch.CloudWatchMetricHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class ActivatorTest {
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private CloudWatchMetricHandler cloudWatchMetricHandler;
    @Mock
    private MetricsEndpoint metricsEndpoint;

    @Test
    void activatorTest() {
        Assertions.assertDoesNotThrow(() -> {
            new SpringAnalyzerActivation(cloudWatchMetricHandler, metricsEndpoint, meterRegistry);
            new SpringAnalyzerClientConfig();
        });
    }
}
