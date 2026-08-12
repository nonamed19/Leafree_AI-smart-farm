package com.leafree.backend.exception;

import com.leafree.backend.exception.type.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException exception) {
        ExceptionCode code = exception.getExceptionCode();
        return ResponseEntity.status(code.getHttpStatus())
                .body(Map.of("code", code.getCode(), "message", code.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ExceptionCode.VALIDATION_ERROR.getMessage());
        return ResponseEntity.badRequest().body(Map.of(
                "code", ExceptionCode.VALIDATION_ERROR.getCode(),
                "message", message
        ));
    }
}
