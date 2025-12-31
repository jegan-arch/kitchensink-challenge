package com.modern.kitchensink.exception;

import lombok.Getter;

@Getter
public class KitchensinkException extends RuntimeException {
    private final ErrorCode errorCode;

    public KitchensinkException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}