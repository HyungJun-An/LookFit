package com.lookfit.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        log.error("BusinessException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(e.getErrorCode(), e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("ValidationException: {}", e.getMessage());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사 실패");
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, message, request.getRequestURI());
        return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        log.error("Exception: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, request.getRequestURI());
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(response);
    }
}
