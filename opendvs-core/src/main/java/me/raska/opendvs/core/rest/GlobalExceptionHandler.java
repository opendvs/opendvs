package me.raska.opendvs.core.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import me.raska.opendvs.core.exception.ApiException;
import me.raska.opendvs.core.exception.ApiException.ValidationError;
import me.raska.opendvs.core.exception.InvalidRequestException;
import me.raska.opendvs.core.exception.NotFoundException;
import me.raska.opendvs.core.exception.UnsupportedTypeActionException;

@Slf4j
@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ InvalidRequestException.class, HttpMessageNotReadableException.class,
            HttpMediaTypeException.class, UnsupportedTypeActionException.class })
    public ApiException handleApiClientException(Exception ex) {
        return getApiException(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiException handleValidationFailureException(MethodArgumentNotValidException ex) {
        return getApiException("Validation failed", HttpStatus.BAD_REQUEST.value(), ex.getBindingResult());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiException handleNotFoundException(Exception ex) {
        return getApiException(ex.getMessage(), HttpStatus.NOT_FOUND.value(), null);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({ AccessDeniedException.class })
    public ApiException handleForbiddenException(Exception ex) {
        return getApiException(ex.getMessage(), HttpStatus.FORBIDDEN.value(), null);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiException handleOtherException(Exception ex, HttpServletRequest request) {
        log.error("Obtained unhandled exception for URI " + request.getRequestURI(), ex);

        return getApiException("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
    }

    public ApiException getApiException(String message, int status, BindingResult binding) {
        return ApiException.builder().message(message).status(status).time(System.currentTimeMillis())
                .validationErrors((binding == null) ? null : getValidationErrors(binding)).build();
    }

    public List<ValidationError> getValidationErrors(BindingResult binding) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (FieldError fe : binding.getFieldErrors()) {
            validationErrors.add(ApiException.ValidationError.builder().object(fe.getObjectName()).code(fe.getCode())
                    .message(fe.getDefaultMessage()).field(fe.getField()).rejectedValue(fe.getRejectedValue()).build());
        }
        return validationErrors;
    }

}
