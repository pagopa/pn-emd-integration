package it.pagopa.pn.emd.integration.config.springbootcfg;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import it.pagopa.pn.common.rest.error.v1.dto.Problem;
import it.pagopa.pn.common.rest.error.v1.dto.ProblemError;
import it.pagopa.pn.commons.exceptions.ExceptionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;

import static it.pagopa.pn.emd.integration.exceptions.PnEmdIntegrationExceptionCodes.PN_EMD_INTEGRATION_INVALID_FORMAT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PnResponseEntityExceptionHandlerActivationTest {

    @Mock
    private ExceptionHelper exceptionHelper;

    @Mock
    private MethodParameter methodParameter;

    private PnResponseEntityExceptionHandlerActivation exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new PnResponseEntityExceptionHandlerActivation(exceptionHelper);
    }

    @Test
    void shouldHandleInvalidFormatExceptionWithSingleField() {
        // Arrange
        List<JsonMappingException.Reference> path = new LinkedList<>();
        path.add(new JsonMappingException.Reference(null, "fieldName"));

        InvalidFormatException invalidFormatException =
                new InvalidFormatException(null, "Invalid format", "invalidValue", String.class);
        setPath(invalidFormatException, path);

        ServerWebInputException serverWebInputException =
                new ServerWebInputException("Input exception", methodParameter, invalidFormatException);

        // Act
        Mono<ResponseEntity<Problem>> result =
                exceptionHandler.handleServerWebInputException(serverWebInputException);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

                    Problem problem = responseEntity.getBody();
                    assertNotNull(problem);
                    assertEquals(PN_EMD_INTEGRATION_INVALID_FORMAT, problem.getType());
                    assertEquals("Invalid Request Format", problem.getTitle());
                    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
                    assertEquals("Request contains fields with invalid format or type.", problem.getDetail());
                    assertNotNull(problem.getTimestamp());

                    assertNotNull(problem.getErrors());
                    assertEquals(1, problem.getErrors().size());

                    ProblemError error = problem.getErrors().get(0);
                    assertEquals(PN_EMD_INTEGRATION_INVALID_FORMAT, error.getCode());
                    assertTrue(error.getDetail().contains("fieldName"));
                    assertTrue(error.getDetail().contains("invalidValue"));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleInvalidFormatExceptionWithNestedPath() {
        // Arrange
        List<JsonMappingException.Reference> path = new LinkedList<>();
        path.add(new JsonMappingException.Reference(null, "parent"));
        path.add(new JsonMappingException.Reference(null, "child"));

        InvalidFormatException invalidFormatException =
                new InvalidFormatException(null, "Invalid format", "testValue", Integer.class);
        setPath(invalidFormatException, path);

        ServerWebInputException serverWebInputException =
                new ServerWebInputException("Input exception", methodParameter, invalidFormatException);

        // Act
        Mono<ResponseEntity<Problem>> result =
                exceptionHandler.handleServerWebInputException(serverWebInputException);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    Problem problem = responseEntity.getBody();
                    ProblemError error = problem.getErrors().get(0);
                    assertTrue(error.getDetail().contains("parent.child"));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleInvalidFormatExceptionWithEmptyPath() {
        // Arrange
        List<JsonMappingException.Reference> path = new LinkedList<>();

        InvalidFormatException invalidFormatException =
                new InvalidFormatException(null, "Invalid format", "someValue", String.class);
        setPath(invalidFormatException, path);

        ServerWebInputException serverWebInputException =
                new ServerWebInputException("Input exception", methodParameter, invalidFormatException);

        // Act
        Mono<ResponseEntity<Problem>> result =
                exceptionHandler.handleServerWebInputException(serverWebInputException);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    Problem problem = responseEntity.getBody();
                    ProblemError error = problem.getErrors().get(0);
                    assertTrue(error.getDetail().contains("unknown"));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleInvalidFormatExceptionWithNullValue() {
        // Arrange
        List<JsonMappingException.Reference> path = new LinkedList<>();
        path.add(new JsonMappingException.Reference(null, "testField"));

        InvalidFormatException invalidFormatException =
                new InvalidFormatException(null, "Invalid format", null, String.class);
        setPath(invalidFormatException, path);

        ServerWebInputException serverWebInputException =
                new ServerWebInputException("Input exception", methodParameter, invalidFormatException);

        // Act
        Mono<ResponseEntity<Problem>> result =
                exceptionHandler.handleServerWebInputException(serverWebInputException);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    Problem problem = responseEntity.getBody();
                    ProblemError error = problem.getErrors().get(0);
                    assertTrue(error.getDetail().contains("'null'"));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleNumberFormatException() {
        // Arrange
        NumberFormatException numberFormatException =
                new NumberFormatException("For input string: \"abc\"");

        ServerWebInputException serverWebInputException =
                new ServerWebInputException("Input exception", methodParameter, numberFormatException);

        // Act
        Mono<ResponseEntity<Problem>> result =
                exceptionHandler.handleServerWebInputException(serverWebInputException);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

                    Problem problem = responseEntity.getBody();
                    assertNotNull(problem);
                    assertEquals(PN_EMD_INTEGRATION_INVALID_FORMAT, problem.getType());
                    assertEquals("Invalid Number Format", problem.getTitle());
                    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
                    assertEquals("Request contains fields with invalid number format.", problem.getDetail());
                    assertNotNull(problem.getTimestamp());

                    assertNotNull(problem.getErrors());
                    assertEquals(1, problem.getErrors().size());

                    ProblemError error = problem.getErrors().get(0);
                    assertEquals(PN_EMD_INTEGRATION_INVALID_FORMAT, error.getCode());
                    assertTrue(error.getDetail().contains("For input string: \"abc\""));
                })
                .verifyComplete();
    }

    @Test
    void shouldDelegateToExceptionHelperForGenericExceptions() {
        // Arrange
        RuntimeException genericException = new RuntimeException("Generic error");

        ServerWebInputException serverWebInputException =
                new ServerWebInputException("Input exception", methodParameter, genericException);

        Problem expectedProblem = Problem.builder()
                .type("GENERIC_ERROR")
                .title("Generic Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("An error occurred")
                .timestamp(OffsetDateTime.now())
                .build();

        when(exceptionHelper.handleException(any(ServerWebInputException.class)))
                .thenReturn(expectedProblem);

        // Act
        Mono<ResponseEntity<Problem>> result =
                exceptionHandler.handleServerWebInputException(serverWebInputException);

        // Assert
        StepVerifier.create(result)
                .assertNext(responseEntity -> {
                    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

                    Problem problem = responseEntity.getBody();
                    assertNotNull(problem);
                    assertEquals("GENERIC_ERROR", problem.getType());
                    assertEquals("Generic Error", problem.getTitle());

                    verify(exceptionHelper, times(1)).handleException(serverWebInputException);
                })
                .verifyComplete();
    }

    // Helper methods

    private void setPath(InvalidFormatException exception, List<JsonMappingException.Reference> path) {
        try {
            java.lang.reflect.Field pathField = JsonMappingException.class.getDeclaredField("_path");
            pathField.setAccessible(true);
            pathField.set(exception, path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set path on InvalidFormatException", e);
        }
    }
}
