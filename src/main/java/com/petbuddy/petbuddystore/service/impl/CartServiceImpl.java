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

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartSession cartSession;

    private final ProductRepository productRepository;

    private final CartMapper cartMapper;

    @Override
    public void addToCart(AddToCartRequest request) {

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
                        .unitPrice(product.getPrice())
                        .quantity(request.getQuantity())
                        .build()
        );
    }

    @Override
    public CartResponse getCart() {

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
