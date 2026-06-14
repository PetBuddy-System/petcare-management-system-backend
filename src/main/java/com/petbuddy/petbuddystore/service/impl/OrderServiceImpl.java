package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.OrderStatus;
import com.petbuddy.petbuddystore.common.enums.ProductStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.CreateOrderRequest;
import com.petbuddy.petbuddystore.dto.response.CartItemResponse;
import com.petbuddy.petbuddystore.dto.response.OrderResponse;
import com.petbuddy.petbuddystore.dto.response.PickingItemResponse;
import com.petbuddy.petbuddystore.dto.response.StaffOrderResponse;
import com.petbuddy.petbuddystore.mapper.OrderMapper;
import com.petbuddy.petbuddystore.model.Order;
import com.petbuddy.petbuddystore.model.OrderDetail;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.model.ProductBatch;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.repository.OrderRepository;
import com.petbuddy.petbuddystore.repository.ProductBatchRepository;
import com.petbuddy.petbuddystore.repository.UserRepository;
import com.petbuddy.petbuddystore.service.CartService;
import com.petbuddy.petbuddystore.service.OrderService;
import com.petbuddy.petbuddystore.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    ProductBatchRepository productBatchRepository;
    ProductService productService;
    CartService cartService;
    OrderMapper orderMapper;
    UserRepository userRepository;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        checkLogin();

        User user = getCurrentUser();
        List<CartItemResponse> cartItems = cartService.getCart().getItems();

        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .recipientName(request.getUserName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .note(request.getNote())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItemResponse item : cartItems) {
            Product product = productService.getProductEntityById(item.getProductId());

            int availableStock = productBatchRepository.findAvailableStockByProductId(product.getProductId());

            if (availableStock < item.getQuantity()) {throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);}

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .productImage(getThumbnail(product))
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
    public void confirmOrder(Long orderId) {
        checkLogin();
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    public void startPicking(Long orderId) {
        checkLogin();
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        order.setStatus(OrderStatus.PICKING);
        orderRepository.save(order);
    }

    @Override
    public List<PickingItemResponse> getPickingList(Long orderId) {
        checkLogin();
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.PICKING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        return buildPickingList(order);
    }

    @Override
    public void shipOrder(Long orderId) {
        checkLogin();
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.PICKING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            deductStockByFefo(detail.getProduct().getProductId(), detail.getQuantity());
        }

        order.setStatus(OrderStatus.SHIPPING);
        orderRepository.save(order);
    }

    @Override
    public void deliveredOrder(Long orderId) {
        checkLogin();
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    @Override
    public void completedOrder(Long orderId) {
        checkLogin();
        Order order = findOrder(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS);
        }
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
    }

    @Override
    public Page<OrderResponse> getOrder(Pageable pageable) {
        User user = getCurrentUser();
        Page<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<StaffOrderResponse> getAllOrder(Pageable pageable) {
        checkLogin();
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toStaffOrderResponse);
    }

    @Override
    public OrderResponse getOrder(Long orderId) {
        getCurrentUser();
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toOrderResponse(order);
    }

    private List<PickingItemResponse> buildPickingList(Order order) {
        List<PickingItemResponse> result = new ArrayList<>();

        for (OrderDetail detail : order.getOrderDetails()) {
            result.addAll(calculatePickingItems(detail.getProduct().getProductId(), detail.getQuantity()));
        }

        return result;
    }

    private List<PickingItemResponse> calculatePickingItems(UUID productId, int quantity) {
        List<ProductBatch> batches = getActiveBatchesByFefo(productId);

        int remaining = quantity;
        List<PickingItemResponse> result = new ArrayList<>();

        for (ProductBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }

            int picked = Math.min(batch.getStockQuantity(), remaining);

            result.add(
                    PickingItemResponse.builder()
                            .productId(batch.getProduct().getProductId())
                            .name(batch.getProduct().getName())
                            .expiryDate(batch.getExpiryDate())
                            .quantityToPick(picked)
                            .build()
            );

            remaining -= picked;
        }

        if (remaining > 0) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        return result;
    }

    private void deductStockByFefo(UUID productId, int quantity) {
        List<ProductBatch> batches = getActiveBatchesByFefo(productId);

        int remaining = quantity;

        for (ProductBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }

            int picked = Math.min(batch.getStockQuantity(), remaining);

            batch.setStockQuantity(batch.getStockQuantity() - picked);
            productBatchRepository.save(batch);

            remaining -= picked;
        }

        if (remaining > 0) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }

    private List<ProductBatch> getActiveBatchesByFefo(UUID productId) {
        return productBatchRepository
                .findByProduct_ProductIdAndStockQuantityGreaterThanAndStatusOrderByExpiryDateAscCreatedAtAscBatchCodeAsc(
                        productId,
                        0,
                        ProductStatus.ACTIVE
                );
    }

    private String getThumbnail(Product product) {
        if (product.getImageUrls() == null || product.getImageUrls().isEmpty()) {
            return null;
        }

        return product.getImageUrls().getFirst();
    }

    private String generateOrderCode() {
        return "OD"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", new Random().nextInt(1000));
    }

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userId = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }


    private void checkLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }
}