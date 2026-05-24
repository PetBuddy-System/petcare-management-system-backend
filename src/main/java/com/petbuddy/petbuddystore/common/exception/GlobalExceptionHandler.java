package com.petbuddy.petbuddystore.common.exception;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse<?>> handlingRuntimeException(RuntimeException exception) {
        log.error("Exception: ", exception);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));

    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse <?>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

//    @ExceptionHandler(value = AccessDeniedException.class)
//    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException ex) {
//        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
////        log.info("AccessDeniedException: {}", ex.getMessage());
//        return ResponseEntity
//                .status(errorCode.getHttpStatusCode())
//                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
//
//    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handlingValidationException(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldErrors().getFirst();
        String enumKey = fieldError.getDefaultMessage();
        ErrorCode errorCode;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INVALID_KEY;
        }

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_DOB_FORMAT;
        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }
}
