package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.AddToCartRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API", description = "Quản lý giỏ hàng")
public class CartController {

    private final CartService cartService;

    @PostMapping
    @Operation(description = "Thêm sản phẩm vào giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @RequestBody @Valid AddToCartRequest request) {

        cartService.addToCart(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product added to cart successfully",
                        null
                )
        );
    }

    @Operation(description = "Get current cart")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart retrieved successfully",
                        cartService.getCart()
                )
        );
    }

    @Operation(description = "Remove product from cart")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long productId) {

        cartService.removeItem(productId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product removed from cart successfully",
                        null
                )
        );
    }

    @Operation(description = "Clear cart")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {

        cartService.clearCart();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart cleared successfully",
                        null
                )
        );
    }
}
