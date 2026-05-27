package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.CartItemRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Cart API", description = "Quản lý giỏ hàng của người dùng")
public class CartController {

    CartService cartService;

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get cart by user",
            description = "Lấy giỏ hàng của người dùng theo userId. Nếu người dùng chưa có giỏ hàng thì hệ thống sẽ tự tạo giỏ hàng mới."
    )
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @Parameter(description = "ID của người dùng", example = "user-123")
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getCartByUserId(userId))
        );
    }

    @PostMapping("/{userId}/items")
    @Operation(
            summary = "Add product to cart",
            description = "Thêm sản phẩm vào giỏ hàng. Nếu sản phẩm đã tồn tại trong giỏ hàng thì hệ thống sẽ cộng thêm số lượng."
    )
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Parameter(description = "ID của người dùng", example = "user-123")
            @PathVariable String userId,

            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Add product to cart successfully",
                        cartService.addToCart(userId, request)
                )
        );
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    @Operation(
            summary = "Update cart item quantity",
            description = "Cập nhật số lượng của một sản phẩm trong giỏ hàng. Quantity phải lớn hơn hoặc bằng 1."
    )
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @Parameter(description = "ID của người dùng", example = "user-123")
            @PathVariable String userId,

            @Parameter(description = "ID của cart item cần cập nhật", example = "1")
            @PathVariable Long cartItemId,

            @Parameter(description = "Số lượng mới của sản phẩm", example = "3")
            @RequestParam @Min(value = 1, message = "QUANTITY_INVALID") Integer quantity
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Update cart item successfully",
                        cartService.updateCartItem(userId, cartItemId, quantity)
                )
        );
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    @Operation(
            summary = "Remove cart item",
            description = "Xóa một sản phẩm khỏi giỏ hàng của người dùng."
    )
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @Parameter(description = "ID của người dùng", example = "user-123")
            @PathVariable String userId,

            @Parameter(description = "ID của cart item cần xóa", example = "1")
            @PathVariable Long cartItemId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Remove cart item successfully",
                        cartService.removeCartItem(userId, cartItemId)
                )
        );
    }

    @DeleteMapping("/{userId}/clear")
    @Operation(
            summary = "Clear cart",
            description = "Xóa toàn bộ sản phẩm trong giỏ hàng của người dùng."
    )
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @Parameter(description = "ID của người dùng", example = "user-123")
            @PathVariable String userId
    ) {
        cartService.clearCart(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Clear cart successfully")
        );
    }
}