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
public class ProductUpdateRequest {

    String name;

    String description;

    BigDecimal price;

    Integer stockQuantity;

    MultipartFile image;

    String brandName;

    Long categoryId;

    @Future(message = "EXPIRY_DATE_INVALID")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate expiryDate;
}