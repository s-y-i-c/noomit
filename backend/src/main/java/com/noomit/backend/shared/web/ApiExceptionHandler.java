package com.noomit.backend.shared.web;

import com.noomit.backend.shared.error.BusinessException;
import com.noomit.backend.shared.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class ApiExceptionHandler {
    record ErrorResponse(boolean success, String errorCode, String message) {}

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusinessException(BusinessException exception) {
        HttpStatus status = switch (exception.errorCode()) {
            case ADMIN_MEMBER_NOT_FOUND, REPAIR_CASE_NOT_FOUND, REPAIR_DETAIL_NOT_FOUND,
                 CUSTOMER_NOT_FOUND, RECEPTION_INVALID_REQUEST, RECEPTION_INVALID_STATUS -> HttpStatus.NOT_FOUND;
            case INVALID_REQUEST, ADMIN_ROLE_INVALID, REPAIR_DETAIL_NOT_IN_CASE -> HttpStatus.BAD_REQUEST;
            case AUTH_EMAIL_ALREADY_EXISTS, ADMIN_SELF_DEMOTION, CUSTOMER_PHONE_ALREADY_EXISTS,
                 RECEPTION_ALREADY_CANCELLED, RECEPTION_CONCURRENT_MODIFICATION, REPAIR_CASE_INVALID_STATUS -> HttpStatus.CONFLICT;
            case AUTH_INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case REPAIR_CASE_ACCESS_DENIED -> HttpStatus.FORBIDDEN;
        };
        return ResponseEntity.status(status).body(
                new ErrorResponse(false, exception.errorCode().name(), exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(false, ErrorCode.AUTH_INVALID_CREDENTIALS.name(), "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleInvalidArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(false, ErrorCode.INVALID_REQUEST.name(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(false, ErrorCode.INVALID_REQUEST.name(),
                exception.getName() + " 값이 올바르지 않습니다."));
    }
}
