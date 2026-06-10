package com.petbuddy.petbuddystore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductPublicResponse {

    UUID productId;
    String name;
    String description;
    BigDecimal price;
    Integer totalStock;

    String imageUrl;
    String brandName;

    Long categoryId;
    String categoryName;

    LocalDate expiryDate; // expiryDate của lô đại diện mới nhất
}