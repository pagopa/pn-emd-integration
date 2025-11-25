package it.pagopa.pn.emd.integration.mapper;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.InlineResponse200;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.v2.model.Outcome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubmitMessageResponseMapperTest {

    @Test
    void testToSendMessageResponse() {
        InlineResponse200 inlineResponse200 = new InlineResponse200();
        inlineResponse200.setOutcome(Outcome.OK);

        SendMessageResponse sendMessageResponse = SubmitMessageResponseMapper.toSendMessageResponse(inlineResponse200);

        assertEquals(SendMessageResponse.OutcomeEnum.OK, sendMessageResponse.getOutcome());
    }
}