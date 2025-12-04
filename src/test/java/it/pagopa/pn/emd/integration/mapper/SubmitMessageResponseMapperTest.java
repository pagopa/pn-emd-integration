package it.pagopa.pn.emd.integration.mapper;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubmitMessageResponseMapperTest {

    @Test
    void testToSendMessageResponse() {
        SubmitMessage200Response SubmitMessage200Response = new SubmitMessage200Response();
        SubmitMessage200Response.setOutcome(Outcome.OK);

        SendMessageResponse sendMessageResponse = SubmitMessageResponseMapper.toSendMessageResponse(SubmitMessage200Response);

        assertEquals(SendMessageResponse.OutcomeEnum.OK, sendMessageResponse.getOutcome());
    }
}