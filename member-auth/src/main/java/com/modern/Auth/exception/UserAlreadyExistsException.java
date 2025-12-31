package com.modern.Auth.exception;

public class UserAlreadyExistsException extends KitchensinkException {
    public UserAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}