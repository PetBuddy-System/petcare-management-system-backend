package com.petbuddy.petbuddystore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class CartItemResponse {

    Long cartItemId;
    Long productId;
    String productName;
    String imageUrl;
    BigDecimal priceAtAdd;
    Integer quantity;
    BigDecimal totalPrice;
}