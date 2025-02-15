package it.pagopa.pn.emd.integration.mapper;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.InlineResponse200;

public class SubmitMessageResponseMapper {

    public static SendMessageResponse toSendMessageResponse(InlineResponse200 submitMessageResponse) {
        SendMessageResponse sendMessageResponse = new SendMessageResponse();
        assert submitMessageResponse.getOutcome() != null;
        sendMessageResponse.setOutcome(SendMessageResponse.OutcomeEnum.valueOf(submitMessageResponse.getOutcome().getValue()));
        return sendMessageResponse;
    }
}