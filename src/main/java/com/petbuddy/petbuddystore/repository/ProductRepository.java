package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCategory_CategoryId(Long categoryId);

    List<Product> findByNameIgnoreCase(String name);

    Optional<Product> findByNameIgnoreCaseAndExpiryDate(String name, LocalDate expiryDate);

    Optional<Product> findFirstByNameIgnoreCaseAndImageUrlIsNotNull(String name);

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByStatusTrueAndDeletedFalse(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatusTrueAndDeletedFalse(
            String keyword,
            Pageable pageable
    );

    Page<Product> findByCategory_CategoryIdAndNameContainingIgnoreCaseAndStatusTrueAndDeletedFalse(
            Long categoryId,
            String keyword,
            Pageable pageable
    );

    Page<Product> findByCategory_CategoryIdAndStatusTrueAndDeletedFalse(
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCaseAndDeletedFalse(
            String keyword,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM Product p WHERE p.name = :name AND p.deleted = false")
    int findTotalStockByName(@Param("name") String name);
    List<Product> findByNameAndStockQuantityGreaterThanAndDeletedFalseOrderByExpiryDateAsc(String name, Integer quantity);
}