package com.petbuddy.petbuddystore.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreationRequest {

    @NotBlank(message = "PRODUCT_NAME_REQUIRED")
    String name;

    String description;

    @NotNull(message = "PRODUCT_PRICE_REQUIRED")
    @DecimalMin(value = "0.0", inclusive = false, message = "PRODUCT_PRICE_INVALID")
    BigDecimal price;

    @NotNull(message = "PRODUCT_STOCK_REQUIRED")
    @Min(value = 0, message = "PRODUCT_STOCK_INVALID")
    Integer stockQuantity;

    MultipartFile image;

    String brandName;

    @NotNull(message = "CATEGORY_ID_REQUIRED")
    Long categoryId;

    @NotNull(message = "EXPIRY_DATE_REQUIRED")
    @Future(message = "EXPIRY_DATE_INVALID")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate expiryDate;
}