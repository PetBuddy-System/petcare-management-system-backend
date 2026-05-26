package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCategory_CategoryId(Long categoryId);
}