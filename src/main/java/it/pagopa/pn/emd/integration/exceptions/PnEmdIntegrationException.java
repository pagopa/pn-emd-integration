package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

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

}
