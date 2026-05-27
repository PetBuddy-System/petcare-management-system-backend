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

    EMAIL_REQUIRED(1002, "Email is required", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    FULL_NAME_REQUIRED(1005, "Full name is required", HttpStatus.BAD_REQUEST),
    GENDER_REQUIRED(1006, "Gender is required", HttpStatus.BAD_REQUEST),
    DATE_OF_BIRTH_REQUIRED(1007, "Date of birth is required", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1008, "User with this email already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1009, "User does not exists", HttpStatus.NOT_FOUND),
    INVALID_DOB_FORMAT(1010, "Date of birth must be in format dd-MM-yyyy", HttpStatus.BAD_REQUEST),

    USER_NOT_VERIFIED(2001, "Please verify your email before logging in", HttpStatus.FORBIDDEN),
    USER_INACTIVE(2002, "User is inactive", HttpStatus.FORBIDDEN),
    USER_SUSPENDED(2003, "User has been suspended", HttpStatus.FORBIDDEN),
    USER_DELETED(2004, "User has been deleted", HttpStatus.GONE),
    UNAUTHENTICATED(2005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2006, "You do not have permission to access", HttpStatus.FORBIDDEN),

    OTP_EXPIRED(3001, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID(3002, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPT(3003, "Too many attempts. Try again later", HttpStatus.BAD_REQUEST),
    OTP_RESEND_COOLDOWN(3004, "Please wait before requesting another OTP", HttpStatus.BAD_REQUEST),
    OTP_RATE_LIMIT(3005, "Too many OTP requests", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED(3006, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_ALREADY_VERIFIED(3007, "Email has already verified", HttpStatus.BAD_REQUEST),
    OTP_REQUIRED(3008, "Otp is required", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(3009, "Old password is incorrect", HttpStatus.BAD_REQUEST),
    PASSWORD_CONFIRM_NOT_MATCH(3010, "Password confirmation does not match", HttpStatus.BAD_REQUEST),
    PASSWORD_SAME_AS_OLD(3011, "New password must be different from old password", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(3012, "Password is required", HttpStatus.BAD_REQUEST),

    CATEGORY_NAME_REQUIRED(4001, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(4002, "Category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(4003, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_HAS_PRODUCTS(4004, "Cannot delete category because it is linked to products", HttpStatus.BAD_REQUEST),
    CATEGORY_DELETED(4005, "Category has been deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_DELETED(4006, "Category is not deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_INACTIVE(4007, "Category is inactive", HttpStatus.BAD_REQUEST),

    PRODUCT_NAME_REQUIRED(5001, "Product name is required", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_REQUIRED(5002, "Product price is required", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_INVALID(5003, "Product price must be greater than 0", HttpStatus.BAD_REQUEST),
    PRODUCT_STOCK_REQUIRED(5004, "Product stock quantity is required", HttpStatus.BAD_REQUEST),
    PRODUCT_STOCK_INVALID(5005, "Product stock quantity must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    PRODUCT_EXISTED(5006, "Product already exists", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(5007, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_DELETED(5008, "Product has been deleted", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_DELETED(5009, "Product is not deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_ID_REQUIRED(5010, "Category id is required", HttpStatus.BAD_REQUEST),
    PRODUCT_STATUS_REQUIRED(5011, "Product status is required", HttpStatus.BAD_REQUEST),;

    int code;
    String message;
    HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
