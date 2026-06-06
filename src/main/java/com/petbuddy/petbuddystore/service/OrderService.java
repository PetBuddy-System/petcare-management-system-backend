package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.CreateOrderRequest;
import com.petbuddy.petbuddystore.dto.response.OrderResponse;
import com.petbuddy.petbuddystore.dto.response.StaffOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    void confirmOrder(Long  orderId);
    void startPicking(Long orderId);
    void shipOrder(Long orderId);
    void cancelOrder(Long orderId);
    Page<OrderResponse> getOrder(Pageable pageable);
    Page<StaffOrderResponse> getAllOrder(Pageable pageable);

}
