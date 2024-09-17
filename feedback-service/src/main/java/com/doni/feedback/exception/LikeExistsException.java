package com.doni.feedback.exception;

public class LikeExistsException extends RuntimeException {
    public LikeExistsException() {
    }

    public LikeExistsException(String message) {
        super(message);
    }

    public LikeExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public LikeExistsException(Throwable cause) {
        super(cause);
    }

    public LikeExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
