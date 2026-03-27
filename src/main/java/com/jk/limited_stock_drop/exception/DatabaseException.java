package com.jk.limited_stock_drop.exception;

import java.io.Serial;

public class DatabaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
