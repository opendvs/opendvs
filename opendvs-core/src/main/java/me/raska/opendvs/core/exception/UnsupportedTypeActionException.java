package me.raska.opendvs.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UnsupportedTypeActionException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -6463355489237204874L;

    public UnsupportedTypeActionException(String msg) {
        super(msg);
    }
}
