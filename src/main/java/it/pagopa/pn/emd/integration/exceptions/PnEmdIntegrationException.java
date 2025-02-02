package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;

@Getter
public class PnEmdIntegrationException extends PnRuntimeException {

    private final String code;

    public PnEmdIntegrationException(String message, int status, String code){
        super(message, message, status, code, null, null);
        this.code = code;
    }

}
