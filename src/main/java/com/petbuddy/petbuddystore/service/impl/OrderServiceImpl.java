package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.OrderStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.CreateOrderRequest;
import com.petbuddy.petbuddystore.dto.response.CartItemResponse;
import com.petbuddy.petbuddystore.dto.response.OrderResponse;
import com.petbuddy.petbuddystore.dto.response.StaffOrderResponse;
import com.petbuddy.petbuddystore.mapper.OrderMapper;
import com.petbuddy.petbuddystore.model.Order;
import com.petbuddy.petbuddystore.model.OrderDetail;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.repository.OrderRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.repository.UserRepository;
import com.petbuddy.petbuddystore.service.CartService;
import com.petbuddy.petbuddystore.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    ProductRepository productRepository;
    CartService cartService;
    OrderMapper orderMapper;
    UserRepository userRepository;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {

         User user = getCurrentUser();

         List<CartItemResponse> cartItems =
                 cartService.getCart().getItems();

         if (cartItems.isEmpty()) {
             throw new AppException(
                     ErrorCode.CART_EMPTY
             );
         }

         Order order = Order.builder()
                 .orderCode(generateOrderCode())
                 .user(user)
                 .recipientName(request.getUserName())
                 .phoneNumber(request.getPhoneNumber())
                 .address(request.getAddress())
                 .note(request.getNote())
                 .status(OrderStatus.PENDING)
                 .build();

         List<OrderDetail> orderDetails = new ArrayList<>();

         BigDecimal total = BigDecimal.ZERO;

         for (CartItemResponse item : cartItems) {

             Product product = productRepository.findById(item.getProductId())
                     .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

             int availableStock = productRepository.findTotalStockByName(item.getProductName());

             if(availableStock < item.getQuantity()){
                 throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
             }

             OrderDetail detail =
                     OrderDetail.builder()
                             .order(order)
                             .product(product)
                             .productName(item.getProductName())
                             .productImage(product.getImageUrl())
                             .unitPrice(item.getPrice())
                             .quantity(item.getQuantity())
                             .totalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                             .build();

             total = total.add(detail.getTotalPrice());
             orderDetails.add(detail);
         }

         order.setOrderDetails(orderDetails);
         order.setTotalAmount(total);
         order.setFinalAmount(total);

         orderRepository.save(order);

         cartService.clearCart();

         return orderMapper.toOrderResponse(order);
     }

    @Override
    public void confirmOrder(Long orderId){

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() != OrderStatus.PENDING){
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    public void startPicking(Long orderId) {
        Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() != OrderStatus.CONFIRMED){
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        for(OrderDetail item : order.getOrderDetails()){
            pickByFEFO(item.getProductName(), item.getQuantity());
        }

        order.setStatus(OrderStatus.PICKING);
    }

    @Override
    public void shipOrder(Long orderId){

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(order.getStatus() != OrderStatus.PICKING){
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        order.setStatus(OrderStatus.SHIPPING);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.CANCELED);
    }

    @Override
    public Page<OrderResponse> getOrder(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Page<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<StaffOrderResponse> getAllOrder(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Page<Order> order = orderRepository.findAll(pageable);
        return order.map(orderMapper::toStaffOrderResponse);
    }

    private void pickByFEFO(String productName, int quantity){

        List<Product> lots = productRepository.findByNameAndDeletedFalseOrderByExpiryDateAsc(productName);

        int remain = quantity;

        for(Product lot : lots){

            if(lot.getStockQuantity() <= 0)
                continue;

            int picked = Math.min(remain, lot.getStockQuantity());

            lot.setStockQuantity(lot.getStockQuantity() - picked);

            remain -= picked;

            productRepository.save(lot);

            if(remain == 0)
                break;
        }

        if(remain > 0){
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }

    private String generateOrderCode() {
        return "OD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000));
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.USER_NOT_EXISTED));
    }
}
