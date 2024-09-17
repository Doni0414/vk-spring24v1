package com.doni.messenger.exception;

public class UserIsNotGroupOwnerException extends RuntimeException {
    public UserIsNotGroupOwnerException() {
    }

    public UserIsNotGroupOwnerException(String message) {
        super(message);
    }

    public UserIsNotGroupOwnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserIsNotGroupOwnerException(Throwable cause) {
        super(cause);
    }

    public UserIsNotGroupOwnerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
