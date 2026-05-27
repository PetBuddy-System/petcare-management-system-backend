package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.CartItemRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;

public interface CartService {

    CartResponse getCartByUserId(String userId);

    CartResponse addToCart(String userId, CartItemRequest request);

    CartResponse updateCartItem(String userId, Long cartItemId, Integer quantity);

    CartResponse removeCartItem(String userId, Long cartItemId);

    void clearCart(String userId);
}