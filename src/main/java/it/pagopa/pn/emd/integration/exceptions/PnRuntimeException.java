package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.emd.integration.exceptions.dto.Problem;
import it.pagopa.pn.emd.integration.exceptions.dto.ProblemError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Getter
public class PnRuntimeException extends RuntimeException {

    private final Problem problem;

    public PnRuntimeException(String message, String description, int status, String errorCode, String element, String detail) {
        super(message);
        ProblemError error = new ProblemError(errorCode, element, detail != null ? detail : description);
        this.problem = Problem.builder()
                .status(status)
                .title(message)
                .detail(description)
                .timestamp(OffsetDateTime.now())
                .errors(Collections.singletonList(error))
                .build();
    }

    public PnRuntimeException(String message, String description, int status, List<ProblemError> errors, String element) {
        super(message);
        this.problem = Problem.builder()
                .status(status)
                .title(message)
                .detail(description)
                .timestamp(OffsetDateTime.now())
                .errors(errors)
                .build();
    }

    public int getStatus() {
        return problem.getStatus() != null ? problem.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
