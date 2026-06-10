package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.CreateOrderRequest;
import com.petbuddy.petbuddystore.dto.response.OrderResponse;
import com.petbuddy.petbuddystore.dto.response.StaffOrderResponse;
import com.petbuddy.petbuddystore.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Order API", description = "Quản lý đơn hàng")
public class OrderController {

    OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(ApiResponse.success("Order created successfully",
                orderService.createOrder(createOrderRequest)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @ParameterObject
            @PageableDefault(
            sort = "createdAt",
            direction = Sort.Direction.DESC
    ) Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully", orderService.getOrder(pageable)));
    }

    @GetMapping("/id")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@RequestParam long id) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderService.getOrder(id)));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmOrder(@PathVariable Long id){
        orderService.confirmOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order confirmed", null));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/picking")
    public ResponseEntity<ApiResponse<Void>> startPicking(@PathVariable Long id){

        orderService.startPicking(id);
        return ResponseEntity.ok(ApiResponse.success("Order picking started", null));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/{id}/picking-list")
    public ResponseEntity<ApiResponse<?>> getPickingList(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.success("Picking list retrieved successfully", orderService.getPickingList(id)));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/shipping")
    public ResponseEntity<ApiResponse<Void>> shipOrder(@PathVariable Long id){

        orderService.shipOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order shipped", null));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/delivered")
    public ResponseEntity<ApiResponse<Void>> deliverOrder(@PathVariable Long id){
        orderService.deliveredOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order delivered", null));
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}/completed")
    public ResponseEntity<ApiResponse<Void>> completedOrder(@PathVariable Long id){
        orderService.completedOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order completed", null));
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id){
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", null));
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/get-all")
    public ResponseEntity<ApiResponse<Page<StaffOrderResponse>>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orderService.getAllOrder(pageable)));
    }
}
