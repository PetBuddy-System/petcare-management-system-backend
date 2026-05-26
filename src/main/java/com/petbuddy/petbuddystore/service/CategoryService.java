package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.CategoryCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CategoryUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryCreationRequest request);

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getActiveCategories();

    List<CategoryResponse> getAllCategoriesForManagement();

    CategoryResponse getCategoryById(Long categoryId);

    CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request);

    CategoryResponse updateCategoryStatus(Long categoryId, Boolean status);

    void softDeleteCategory(Long categoryId);

    CategoryResponse restoreCategory(Long categoryId);
}