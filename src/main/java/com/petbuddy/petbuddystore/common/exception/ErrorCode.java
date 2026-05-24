package com.petbuddy.petbuddystore.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(9998, "Uncategorized error", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(1001, "You do not have permission to access", HttpStatus.FORBIDDEN),
    EMAIL_REQUIRED(1002, "Email is required", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    FULL_NAME_REQUIRED(1005, "Full name is required", HttpStatus.BAD_REQUEST),
    GENDER_REQUIRED(1006, "Gender is required", HttpStatus.BAD_REQUEST),
    DATE_OF_BIRTH_REQUIRED(1007, "Date of birth is required", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1008, "User with this email already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1009, "User does not exists", HttpStatus.NOT_FOUND),
    INVALID_DOB_FORMAT(1010, "Date of birth must be in format dd-MM-yyyy", HttpStatus.BAD_REQUEST),


    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
