package com.doni.messenger.exception;

public class ChatExistsException extends RuntimeException {
    public ChatExistsException() {
    }

    public ChatExistsException(String message) {
        super(message);
    }

    public ChatExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChatExistsException(Throwable cause) {
        super(cause);
    }

    public ChatExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
