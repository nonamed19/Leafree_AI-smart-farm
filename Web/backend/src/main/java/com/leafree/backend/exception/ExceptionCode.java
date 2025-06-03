package com.leafree.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {
    // 인증 관련 (1000번대)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", 1000),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", 1001),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.", 1002),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다.", 1003),
    
    // 사용자 관련 (2000번대)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.", 2000),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.", 2001),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다.", 2002),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.", 2003),
    
    // 디바이스 관련 (3000번대)
    DEVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "디바이스를 찾을 수 없습니다.", 3000),
    INVALID_DEVICE_UUID(HttpStatus.BAD_REQUEST, "유효하지 않은 디바이스 UUID입니다.", 3001),
    DEVICE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 등록된 디바이스입니다.", 3002),
    DEVICE_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "등록되지 않은 디바이스입니다.", 3003),
    
    // 작물 관련 (4000번대)
    CROP_NOT_FOUND(HttpStatus.NOT_FOUND, "작물을 찾을 수 없습니다.", 4000),
    INVALID_CROP_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 작물 상태입니다.", 4001),
    CROP_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 재배 중인 작물이 있습니다.", 4002),
    
    // 센서 관련 (5000번대)
    SENSOR_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "센서 데이터를 찾을 수 없습니다.", 5000),
    INVALID_SENSOR_DATA(HttpStatus.BAD_REQUEST, "유효하지 않은 센서 데이터입니다.", 5001),
    
    // 권한 관련 (6000번대)
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", 6000),
    DEVICE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 디바이스에 대한 접근 권한이 없습니다.", 6001),
    
    // 유효성 검사 관련 (7000번대)
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다.", 7000),
    
    // 서버 관련 (9000번대)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", 9000),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다.", 9001);

    private final HttpStatus httpStatus;
    private final String message;
    private final int code;

    ExceptionCode(HttpStatus httpStatus, String message, int code) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.code = code;
    }
}
