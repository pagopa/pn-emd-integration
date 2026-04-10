package it.pagopa.pn.emd.integration.config.aws;

import lombok.Data;

@Data
public class AwsConfigs {
    private String profileName;
    private String regionCode;
    private String endpointUrl;
}
