package it.pagopa.pn.emd.integration.exceptions.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProblemError {

    private String code;
    private String element;
    private String detail;

    public ProblemError code(String code) {
        this.code = code;
        return this;
    }

    public ProblemError element(String element) {
        this.element = element;
        return this;
    }

    public ProblemError detail(String detail) {
        this.detail = detail;
        return this;
    }
}
