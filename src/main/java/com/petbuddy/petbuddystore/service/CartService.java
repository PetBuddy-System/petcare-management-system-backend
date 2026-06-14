package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.AddToCartRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    void addToCart(AddToCartRequest request);

    CartResponse getCart();

    void removeItem(UUID productId);

    void clearCart();
}
