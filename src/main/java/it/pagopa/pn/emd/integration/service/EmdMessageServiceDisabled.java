package it.pagopa.pn.emd.integration.service;

import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.SubmitMessage200Response;
import it.pagopa.pn.emdintegration.generated.openapi.msclient.emdcoreclient.model.Outcome;
import it.pagopa.pn.emdintegration.generated.openapi.server.v1.dto.SendMessageRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@ConditionalOnProperty(
        name = "pn.emd-integration.message.enabled",
        havingValue = "false"
)
@Service
@Slf4j
public class EmdMessageServiceDisabled implements EmdMessageService {
    @Override
    public Mono<SubmitMessage200Response> submitMessage(SendMessageRequestBody request) {
        log.info("[Message Service disabled] - Start submitMessage for request: {}", request);
        return Mono.just(new SubmitMessage200Response(Outcome.NO_CHANNELS_ENABLED));
    }
}
