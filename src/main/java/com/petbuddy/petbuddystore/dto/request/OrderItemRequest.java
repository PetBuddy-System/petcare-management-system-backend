package com.petbuddy.petbuddystore.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemRequest {
    private UUID productId;
    private BigDecimal price;
    private Integer quantity;
}
