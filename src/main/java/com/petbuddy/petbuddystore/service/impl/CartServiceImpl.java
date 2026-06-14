package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.AddToCartRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.mapper.CartMapper;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.ProductBatchRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.CartService;
import com.petbuddy.petbuddystore.service.ProductService;
import com.petbuddy.petbuddystore.session.CartItemSession;
import com.petbuddy.petbuddystore.session.CartSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartSession cartSession;

    ProductService productService;

    ProductBatchRepository productBatchRepository;

    CartMapper cartMapper;

    @Override
    public void addToCart(AddToCartRequest request) {

        checkLogin();

        Product product = productService.getProductEntityById(
                request.getProductId()
        );

        int availableStock =
                productBatchRepository.findAvailableStockByProductId(
                        product.getProductId()
                );

        if (availableStock < request.getQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        CartItemSession existingItem =
                cartSession.getItems()
                        .stream()
                        .filter(item ->
                                item.getProductId()
                                        .equals(product.getProductId()))
                        .findFirst()
                        .orElse(null);

        if (existingItem != null) {

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + request.getQuantity());

            existingItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
            return;
        }

        cartSession.getItems().add(
                CartItemSession.builder()
                        .productId(product.getProductId())
                        .productName(product.getName())
                        .price(product.getPrice())
                        .quantity(request.getQuantity())
                        .subtotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                        .build()
        );
    }

    @Override
    public CartResponse getCart() {
        checkLogin();
        return cartMapper.toCartResponse(cartSession);
    }

    @Override
    public void removeItem(UUID productId) {

        checkLogin();

        cartSession.getItems()
                .removeIf(item ->
                        item.getProductId()
                                .equals(productId));
    }

    @Override
    public void clearCart() {
        cartSession.getItems().clear();
    }

    private void checkLogin() {
        String userId = cartSession.getUserId();

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
