package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class EmdMessageServiceDisabledTest {
    private final EmdMessageServiceDisabled emdMessageServiceDisabled = new EmdMessageServiceDisabled();

    @Test
    void submitMessageReturnsNoChannelsEnabled() {
        SendMessageRequestBody requestBody = new SendMessageRequestBody();
        requestBody.setRecipientId("recipientId");
        requestBody.setDeliveryMode(SendMessageRequestBody.DeliveryModeEnum.DIGITAL);

        Mono<InlineResponse200> result = emdMessageServiceDisabled.submitMessage(requestBody);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getOutcome() == Outcome.NO_CHANNELS_ENABLED)
                .verifyComplete();
    }
}
