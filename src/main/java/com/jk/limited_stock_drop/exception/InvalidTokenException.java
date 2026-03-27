package com.jk.limited_stock_drop.exception;

import java.io.Serial;

public class InvalidTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String message) {
        super(message);
    }
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
