package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import it.pagopa.pn.commons.exceptions.dto.ProblemError;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.List;

@Getter
public class PnEmdIntegrationException extends PnRuntimeException {

    private final String code;

    public PnEmdIntegrationException(String message, int status, String code){
        super(message, message, status, code, null, null);
        this.code = code;
    }

    public PnEmdIntegrationException(String message, String code){
        super(message, message, HttpStatus.INTERNAL_SERVER_ERROR.value(), code, null, null);
        this.code = code;
    }
    public PnEmdIntegrationException(String message, String detail, String code, String element) {
        super(
                message,
                detail,
                HttpStatus.BAD_REQUEST.value(),
                List.of(new ProblemError(code, element, detail)),
                null
             );
        this.code = code;
    }

}
