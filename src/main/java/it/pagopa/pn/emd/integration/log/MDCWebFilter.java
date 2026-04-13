package it.pagopa.pn.emd.integration.log;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class MDCWebFilter implements WebFilter, Ordered {

    public static final String TRACE_ID_HEADER = "x-pagopa-pn-cx-id";
    public static final String TRACE_ID_MDC_KEY = "trace_id";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        String finalTraceId = traceId;
        MDC.put(TRACE_ID_MDC_KEY, finalTraceId);
        return chain.filter(exchange)
                .doFinally(s -> MDC.remove(TRACE_ID_MDC_KEY));
    }
}
