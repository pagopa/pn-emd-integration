package it.pagopa.pn.emd.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class AccessTokenRequestDto {
    private String clientId;
    private String clientSecret;
}
