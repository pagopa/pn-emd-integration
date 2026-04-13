package it.pagopa.pn.emd.integration.pnclients;

import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

public abstract class CommonBaseClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    protected WebClient initWebClient(WebClient.Builder builder) {
        return builder
                .filter((request, next) -> next.exchange(request)
                        .timeout(DEFAULT_TIMEOUT))
                .build();
    }
}
