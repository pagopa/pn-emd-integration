package it.pagopa.pn.emd.integration.config.springbootcfg;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.common.rest.error.v1.dto.ProblemError;
import it.pagopa.pn.commons.exceptions.ExceptionHelper;
import it.pagopa.pn.commons.exceptions.PnResponseEntityExceptionHandler;
import lombok.CustomLog;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_INVALID_FORMAT;

@CustomLog
@org.springframework.web.bind.annotation.ControllerAdvice
@Import(ExceptionHelper.class)
public class PnResponseEntityExceptionHandlerActivation extends PnResponseEntityExceptionHandler {

    private final ExceptionHelper exceptionHelper;

    public PnResponseEntityExceptionHandlerActivation(ExceptionHelper exceptionHelper) {
        super(exceptionHelper);
        this.exceptionHelper = exceptionHelper;
    }

    /**
     * Questo handler è attualmente necessario perché ExceptionHelper restituisce un Problem
     * senza dettagli specifici per errori di formato (es. InvalidFormatException).
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Problem>> handleServerWebInputException(ServerWebInputException ex) {
        Problem problem;
        if (ex.getMostSpecificCause() instanceof InvalidFormatException invalidFormatException)
            problem = handleInvalidFormatException(invalidFormatException);
        else if (ex.getMostSpecificCause() instanceof NumberFormatException numberFormatException) {
            problem = handleNumberFormatException(numberFormatException);
        } else problem = exceptionHelper.handleException(ex);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem));
    }

    private Problem handleInvalidFormatException(InvalidFormatException ex) {
        String fieldName = ex.getPath().stream().map(JsonMappingException.Reference::getFieldName).filter(name -> name != null && !name.isEmpty()).collect(Collectors.joining("."));

        if (fieldName.isEmpty()) {
            fieldName = "unknown";
        }

        String invalidValue = ex.getValue() == null ? "null" : ex.getValue().toString();

        ProblemError error = new ProblemError().code(PN_EMD_INTEGRATION_INVALID_FORMAT).detail(String.format("Invalid format for field '%s': value '%s' is not valid.", fieldName, invalidValue));

        return Problem.builder().type(PN_EMD_INTEGRATION_INVALID_FORMAT).title("Invalid Request Format").status(HttpStatus.BAD_REQUEST.value()).detail("Request contains fields with invalid format or type.").timestamp(OffsetDateTime.now()).errors(Collections.singletonList(error)).build();

    }

    private Problem handleNumberFormatException(NumberFormatException ex) {
        ProblemError error = new ProblemError().code(PN_EMD_INTEGRATION_INVALID_FORMAT).detail("Invalid number format: " + ex.getMessage());

        return new Problem().type(PN_EMD_INTEGRATION_INVALID_FORMAT).title("Invalid Number Format").status(HttpStatus.BAD_REQUEST.value()).detail("Request contains fields with invalid number format.").timestamp(OffsetDateTime.now()).errors(Collections.singletonList(error));
    }
}