package com.leafree.backend.exception.type;

import com.leafree.backend.exception.ExceptionCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ExceptionCode exceptionCode;

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }
}
