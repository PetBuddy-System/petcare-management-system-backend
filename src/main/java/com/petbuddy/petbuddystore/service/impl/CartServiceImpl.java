package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.CartItemRequest;
import com.petbuddy.petbuddystore.dto.response.CartItemResponse;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.model.Cart;
import com.petbuddy.petbuddystore.model.CartItem;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.repository.CartItemRepository;
import com.petbuddy.petbuddystore.repository.CartRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.repository.UserRepository;
import com.petbuddy.petbuddystore.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;

    @Override
    public CartResponse getCartByUserId(String userId) {
        Cart cart = getOrCreateCart(userId);
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = getActiveProduct(request.getProductId());

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_STOCK_INVALID);
        }

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem == null) {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .priceAtAdd(product.getPrice())
                    .build();

            cart.getCartItems().add(cartItem);
        } else {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();

            if (newQuantity > product.getStockQuantity()) {
                throw new AppException(ErrorCode.PRODUCT_STOCK_INVALID);
            }

            cartItem.setQuantity(newQuantity);
        }

        return toCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userId, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new AppException(ErrorCode.QUANTITY_INVALID);
        }

        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository
                .findByCartCartIdAndCartItemId(cart.getCartId(), cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        Product product = cartItem.getProduct();

        if (quantity > product.getStockQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_STOCK_INVALID);
        }

        cartItem.setQuantity(quantity);

        return toCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(String userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository
                .findByCartCartIdAndCartItemId(cart.getCartId(), cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return toCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

                    Cart cart = Cart.builder()
                            .user(user)
                            .cartItems(new ArrayList<>())
                            .build();

                    return cartRepository.save(cart);
                });
    }

    private Product getActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            throw new AppException(ErrorCode.PRODUCT_DELETED);
        }

        if (Boolean.FALSE.equals(product.getStatus())) {
            throw new AppException(ErrorCode.PRODUCT_STATUS_REQUIRED);
        }

        return product;
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUser().getUserId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal totalPrice = item.getPriceAtAdd()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getName())
                .imageUrl(item.getProduct().getImageUrl())
                .priceAtAdd(item.getPriceAtAdd())
                .quantity(item.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }
}