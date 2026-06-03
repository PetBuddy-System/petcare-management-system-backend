package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    List<Category> findByDeletedFalse();

    List<Category> findByStatusTrueAndDeletedFalse();

    List<Category> findByDeletedTrue();
}