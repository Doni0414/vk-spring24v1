package com.doni.messenger.exception;

public class UserIsAlreadyGroupMemberException extends RuntimeException {
    public UserIsAlreadyGroupMemberException() {
    }

    public UserIsAlreadyGroupMemberException(String message) {
        super(message);
    }

    public UserIsAlreadyGroupMemberException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserIsAlreadyGroupMemberException(Throwable cause) {
        super(cause);
    }

    public UserIsAlreadyGroupMemberException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
