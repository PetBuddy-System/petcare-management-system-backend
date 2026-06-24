package com.petbuddy.petbuddystore.dto.cart;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartData {
    private String userId;

    @Builder.Default
    private List<CartItemData> items = new ArrayList<>();
}
