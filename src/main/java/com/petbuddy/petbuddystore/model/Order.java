package com.petbuddy.petbuddystore.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    Long orderId;

    @Column(unique = true)
    String orderCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Column(columnDefinition = "NVARCHAR(255)")
    String recipientName;

    String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(100)")
    String province;

    @Column(columnDefinition = "NVARCHAR(100)")
    String district;

    @Column(columnDefinition = "NVARCHAR(100)")
    String ward;

    @Column(columnDefinition = "NVARCHAR(500)")
    String detailAddress;

    BigDecimal shippingFee;

    BigDecimal totalAmount;

    BigDecimal finalAmount;

    @Column(columnDefinition = "NVARCHAR(500)")
    String note;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    User assignedStaff;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}