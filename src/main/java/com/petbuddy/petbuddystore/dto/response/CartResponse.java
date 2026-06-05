package com.petbuddy.petbuddystore.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class CartResponse {
    private String userId;

    private List<CartItemResponse> items;
}
