package it.pagopa.pn.emd.integration.mapper;

import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageResponse;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;

public class SubmitMessageResponseMapper {
    private SubmitMessageResponseMapper() {}

    public static SendMessageResponse toSendMessageResponse(SubmitMessage200Response submitMessageResponse) {
        SendMessageResponse sendMessageResponse = new SendMessageResponse();
        sendMessageResponse.setOutcome(SendMessageResponse.OutcomeEnum.valueOf(submitMessageResponse.getOutcome().getValue()));
        return sendMessageResponse;
    }
}