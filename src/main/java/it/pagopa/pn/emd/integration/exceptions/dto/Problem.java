package it.pagopa.pn.emd.integration.exceptions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Problem {

    private String type;
    private String title;
    private Integer status;
    private String detail;
    private OffsetDateTime timestamp;
    private List<ProblemError> errors;

    public Problem type(String type) {
        this.type = type;
        return this;
    }

    public Problem title(String title) {
        this.title = title;
        return this;
    }

    public Problem status(Integer status) {
        this.status = status;
        return this;
    }

    public Problem detail(String detail) {
        this.detail = detail;
        return this;
    }

    public Problem timestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Problem errors(List<ProblemError> errors) {
        this.errors = errors;
        return this;
    }
}
