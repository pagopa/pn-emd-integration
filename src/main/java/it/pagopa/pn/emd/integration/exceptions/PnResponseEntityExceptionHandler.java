package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.emd.integration.exceptions.dto.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

public abstract class PnResponseEntityExceptionHandler {

    private final ExceptionHelper exceptionHelper;

    protected PnResponseEntityExceptionHandler(ExceptionHelper exceptionHelper) {
        this.exceptionHelper = exceptionHelper;
    }

    @ExceptionHandler(PnRuntimeException.class)
    public Mono<ResponseEntity<Problem>> handlePnRuntimeException(PnRuntimeException ex) {
        Problem problem = ex.getProblem();
        return Mono.just(ResponseEntity.status(ex.getStatus()).body(problem));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Problem>> handleGenericException(Exception ex) {
        Problem problem = exceptionHelper.handleException(ex);
        int status = problem.getStatus() != null ? problem.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR.value();
        return Mono.just(ResponseEntity.status(status).body(problem));
    }
}
