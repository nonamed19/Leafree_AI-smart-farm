package com.leafree.backend.exception.type;

import com.leafree.backend.exception.ExceptionCode;

public class DbException extends BusinessException {
    public DbException(ExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
