package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.AddToCartRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.mapper.CartMapper;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.CartService;
import com.petbuddy.petbuddystore.session.CartItemSession;
import com.petbuddy.petbuddystore.session.CartSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartSession cartSession;

    private final ProductRepository productRepository;

    private final CartMapper cartMapper;

    @Override
    public void addToCart(AddToCartRequest request) {

        String userId = cartSession.getUserId();

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.PRODUCT_NOT_FOUND));

        CartItemSession existingItem =
                cartSession.getItems()
                        .stream()
                        .filter(item ->
                                item.getProductId()
                                        .equals(product.getProductId()))
                        .findFirst()
                        .orElse(null);

        if(existingItem != null){

            existingItem.setQuantity(
                    existingItem.getQuantity()
                            + request.getQuantity());

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
        String userId = cartSession.getUserId();

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return cartMapper.toCartResponse(cartSession);
    }

    @Override
    public void removeItem(Integer productId) {

        cartSession.getItems()
                .removeIf(item ->
                        item.getProductId()
                                .equals(productId));
    }

    @Override
    public void clearCart() {

        cartSession.getItems().clear();
    }
}
