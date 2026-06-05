package com.petbuddy.petbuddystore.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {
    private Long productId;

    private Integer quantity;
}
