package com.doni.publication.exception;

public class UserIsNotOwnerException extends RuntimeException {
    public UserIsNotOwnerException() {
    }

    public UserIsNotOwnerException(String message) {
        super(message);
    }

    public UserIsNotOwnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserIsNotOwnerException(Throwable cause) {
        super(cause);
    }

    public UserIsNotOwnerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
