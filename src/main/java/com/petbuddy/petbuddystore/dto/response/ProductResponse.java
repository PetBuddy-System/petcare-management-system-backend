package com.petbuddy.petbuddystore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    Long productId;
    String name;
    String description;
    BigDecimal price;
    Integer stockQuantity;
    String imageUrl;
    String brandName;
    Boolean status;
    Boolean deleted;
    LocalDateTime deletedAt;

    Long categoryId;
    String categoryName;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}