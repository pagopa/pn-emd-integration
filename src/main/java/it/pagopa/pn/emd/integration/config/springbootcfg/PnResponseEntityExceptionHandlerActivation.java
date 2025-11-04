package it.pagopa.pn.emd.integration.config.springbootcfg;

import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.commons.exceptions.ExceptionHelper;
import it.pagopa.pn.commons.exceptions.PnResponseEntityExceptionHandler;
import lombok.CustomLog;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;

@CustomLog
@org.springframework.web.bind.annotation.ControllerAdvice
@Import(ExceptionHelper.class)
public class PnResponseEntityExceptionHandlerActivation extends PnResponseEntityExceptionHandler {

    private final ExceptionHelper exceptionHelper;

    public PnResponseEntityExceptionHandlerActivation(ExceptionHelper exceptionHelper) {
        super(exceptionHelper);
        this.exceptionHelper = exceptionHelper;
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Problem>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("ServerWebInputException caught: {}", ex.getMessage(), ex);
        Problem problem = exceptionHelper.handleException(ex);

        if (ex.getReason() != null) {

            List<it.pagopa.pn.common.rest.error.v1.dto.ProblemError> errors = new ArrayList<>();
            errors.add(new it.pagopa.pn.common.rest.error.v1.dto.ProblemError(
                    "PN_GENERIC_INVALIDPARAMETER_PATTERN",
                    ex.getMostSpecificCause().getMessage(),
                    ex.getCause().getMessage()));
            problem.setErrors(errors);

        }

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }
}