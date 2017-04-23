package me.raska.opendvs.core.exception;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiException {
    private final int status;
    private final String message;
    private List<ValidationError> validationErrors;
    private long time;

    @Getter
    @Builder
    public static class ValidationError {
        private String object;
        private String code;
        private String message;
        private String field;
        private Object rejectedValue;
    }
}
