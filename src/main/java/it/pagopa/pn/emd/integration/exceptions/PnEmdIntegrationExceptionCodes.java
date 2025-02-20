package it.pagopa.pn.emd.integration.exceptions;

import it.pagopa.pn.commons.exceptions.PnExceptionsCodes;

public class PnEmdIntegrationExceptionCodes extends PnExceptionsCodes {
    public static final String MIL_AUTH_ERROR = "PN_EMD_INTEGRATION_MIL_AUTH_ERROR";
    public static final String PN_EMD_INTEGRATION_SEND_MESSAGE_ERROR = "PN_EMD_INTEGRATION_SEND_MESSAGE_ERROR";
    public static final String PN_EMD_INTEGRATION_RETRIEVAL_PAYLOAD_MISSING_OR_EXPIRED = "PN_EMD_INTEGRATION_RETRIEVAL_PAYLOAD_MISSING_OR_EXPIRED";
    public static final String PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR = "PN_EMD_INTEGRATION_SERVICE_DISABLED_ERROR";
}