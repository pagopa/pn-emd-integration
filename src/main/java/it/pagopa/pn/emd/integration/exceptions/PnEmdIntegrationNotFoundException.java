package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PnEmdIntegrationNotFoundException extends PnRuntimeException {

    public PnEmdIntegrationNotFoundException(String message, String description, String errorcode) {
        super(message, description, HttpStatus.NOT_FOUND.value(), errorcode, null, description);
    }
}
