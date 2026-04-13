package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationNotFoundException;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.RetrievalPayload;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EmdRetrievalServiceDisabledTest {
    private final EmdRetrievalServiceDisabled emdRetrievalServiceDisabled = new EmdRetrievalServiceDisabled();

    @Test
    void getTokenRetrievalPayloadServiceDisabled() {
        String retrievalId = "retrievalId";

        Mono<RetrievalPayload> result = emdRetrievalServiceDisabled.getTokenRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }

    @Test
    void getEmdRetrievalPayloadServiceDisabled() {
        String retrievalId = "retrievalId";

        Mono<RetrievalPayload> result = emdRetrievalServiceDisabled.getEmdRetrievalPayload(retrievalId);

        StepVerifier.create(result)
                .expectError(PnEmdIntegrationNotFoundException.class)
                .verify();
    }
}
