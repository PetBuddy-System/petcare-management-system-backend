package com.petbuddy.petbuddystore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    Long productId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    String name;

    @Column(columnDefinition = "NVARCHAR(2000)")
    String description;

    @Column(nullable = false)
    BigDecimal price;

    @Column(nullable = false)
    @Min(value = 0, message = "PRODUCT_STOCK_INVALID")
    Integer stockQuantity;

    @Column(columnDefinition = "TEXT")
    String imageUrl;

    @Column(name = "image_key")
    String imageKey;

    @Column(columnDefinition = "NVARCHAR(100)")
    String brandName;

    @Builder.Default
    Boolean status = true;

    @Builder.Default
    Boolean deleted = false;

    LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;


    @Builder.Default
    @OneToMany(mappedBy = "product")
    List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    @Column(name = "expiry_date")
    LocalDate expiryDate;
}