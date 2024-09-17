package com.doni.message.exception;

public class UserIsNotChatParticipantException extends RuntimeException {
    public UserIsNotChatParticipantException() {
    }

    public UserIsNotChatParticipantException(String message) {
        super(message);
    }

    public UserIsNotChatParticipantException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserIsNotChatParticipantException(Throwable cause) {
        super(cause);
    }

    public UserIsNotChatParticipantException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
