package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.AddToCartRequest;
import com.petbuddy.petbuddystore.dto.request.UpdateCartItemRequest;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.mapper.CartMapper;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.CartService;
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

    ProductRepository productRepository;

    CartMapper cartMapper;

    @Override
    public void addToCart(AddToCartRequest request) {
        checkLogin();

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        CartItemSession existingItem = cartSession.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(product.getProductId())
                        && item.getPrice().compareTo(product.getPrice()) == 0)
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotal(existingItem.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            return;
        }

        cartSession.getItems().add(
                CartItemSession.builder()
                        .cartItemId(UUID.randomUUID())
                        .productId(product.getProductId())
                        .productName(product.getName())
                        .price(product.getPrice())
                        .quantity(request.getQuantity())
                        .subtotal(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())))
                        .build());
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

    @Override
    public void updateCart(UUID cartItemId, UpdateCartItemRequest request) {
        checkLogin();

        CartItemSession item = cartSession.getItems()
                .stream()
                .filter(i -> i.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        boolean priceChanged = item.getPrice().compareTo(product.getPrice()) != 0;

        if (priceChanged) {
            if (Boolean.FALSE.equals(request.getAcceptPriceChange())) {
                throw new AppException(ErrorCode.PRODUCT_PRICE_CHANGE);
            }
            item.setPrice(product.getPrice());
            item.setProductName(product.getName());
        }
        item.setQuantity(request.getQuantity());
        item.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }

    private void checkLogin() {
        String userId = cartSession.getUserId();

        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

    }
}
