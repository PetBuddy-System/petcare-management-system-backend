package com.petbuddy.petbuddystore.dto.response;

import com.petbuddy.petbuddystore.common.enums.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductPromotionResponse {

    UUID productId;

    String productCode;

    String name;

    BigDecimal price;

    String brandName;

    ProductStatus status;

    Long totalStock;

    Long nearExpiredStock;

    Long nearExpiredBatchCount;

    LocalDate nearestExpiryDate;

    Boolean hasActivePromotion;
}