package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.emd.integration.exceptions.dto.Problem;
import it.pagopa.pn.emd.integration.exceptions.dto.ProblemError;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Collections;

@Component
public class ExceptionHelper {

    public Problem handleException(Throwable ex) {
        if (ex instanceof PnRuntimeException pnEx) {
            return pnEx.getProblem();
        }
        if (ex instanceof ResponseStatusException responseStatusEx) {
            int status = responseStatusEx.getStatusCode().value();
            return Problem.builder()
                    .status(status)
                    .title(HttpStatus.valueOf(status).getReasonPhrase())
                    .detail(responseStatusEx.getReason())
                    .timestamp(OffsetDateTime.now())
                    .errors(Collections.emptyList())
                    .build();
        }
        return Problem.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .title("Internal Server Error")
                .detail(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .errors(Collections.singletonList(
                        new ProblemError("PN_GENERIC_ERROR", null, ex.getMessage())
                ))
                .build();
    }
}
