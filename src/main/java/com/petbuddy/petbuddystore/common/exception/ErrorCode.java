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

    CATEGORY_NAME_REQUIRED(4101, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(4102, "Category already exists", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(4103, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_HAS_PRODUCTS(4104, "Cannot delete category because it is linked to products", HttpStatus.BAD_REQUEST),
    CATEGORY_DELETED(4105, "Category has been deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_DELETED(4106, "Category is not deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_INACTIVE(4107, "Category is inactive", HttpStatus.BAD_REQUEST),

    PRODUCT_NAME_REQUIRED(4201, "Product name is required", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_REQUIRED(4202, "Product price is required", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_INVALID(4203, "Product price must be greater than 0", HttpStatus.BAD_REQUEST),
    PRODUCT_STOCK_REQUIRED(4204, "Product stock quantity is required", HttpStatus.BAD_REQUEST),
    PRODUCT_STOCK_INVALID(4205, "Product stock quantity must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    PRODUCT_EXISTED(4206, "Product already exists", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(4207, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_DELETED(4208, "Product has been deleted", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_DELETED(4209, "Product is not deleted", HttpStatus.BAD_REQUEST),
    CATEGORY_ID_REQUIRED(4210, "Category id is required", HttpStatus.BAD_REQUEST),
    PRODUCT_STATUS_REQUIRED(4211, "Product status is required", HttpStatus.BAD_REQUEST),
    PRODUCT_IMAGE_REQUIRED(4212, "Product image is required", HttpStatus.BAD_REQUEST),
    EXPIRY_DATE_REQUIRED(4213, "Expiry date is required", HttpStatus.BAD_REQUEST),
    EXPIRY_DATE_INVALID(4214, "Expiry date must be in the future", HttpStatus.BAD_REQUEST),

    FILE_REQUIRED(7001, "File is required", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(7002, "File size must be <= 5MB", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(7003, "Only JPG, PNG, WEBP images are allowed", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(7004, "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    IMAGE_SIZE_INVALID(7005, "Upload maximum 3 photos", HttpStatus.BAD_REQUEST),
    DELETE_FILE_FAILED(7006, "Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EMPTY(7007, "File is empty", HttpStatus.BAD_REQUEST),
    IMPORT_FAILED(7008, "Failed to import file. Please check the file format and data.", HttpStatus.BAD_REQUEST),
    INVALID_EXCEL_TEMPLATE(7009, "Invalid Excel template. Please use the provided template.", HttpStatus.BAD_REQUEST),

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
