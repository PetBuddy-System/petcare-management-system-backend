package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.AddToCartRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;

public interface CartService {
    void addToCart(AddToCartRequest request);

    CartResponse getCart();

    void removeItem(Long productId);

    void clearCart();
}
