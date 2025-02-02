package it.pagopa.pn.emd.integration.rest;

import it.pagopa.pn.emd.integration.generated.openapi.server.v1.api.TemplateSampleApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class PnEmdIntegrationController implements TemplateSampleApi {

    @Override
    public Mono<ResponseEntity<Map<String, List<String>>>> getHttpHeadersMap(ServerWebExchange exchange) {


        return Mono.fromSupplier(() ->{
            log.debug("Start getHttpHeadersMap");
            Map<String, List<String>> headers = new HashMap<>(exchange.getRequest().getHeaders());
            return ResponseEntity.ok(headers);
        });

    }
}
