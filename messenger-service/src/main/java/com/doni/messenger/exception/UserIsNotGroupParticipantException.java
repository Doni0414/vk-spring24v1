package com.doni.messenger.exception;

public class UserIsNotGroupParticipantException extends RuntimeException {
    public UserIsNotGroupParticipantException() {
    }

    public UserIsNotGroupParticipantException(String message) {
        super(message);
    }

    public UserIsNotGroupParticipantException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserIsNotGroupParticipantException(Throwable cause) {
        super(cause);
    }

    public UserIsNotGroupParticipantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
