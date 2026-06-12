package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.CategoryCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CategoryUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CategoryResponse;
import com.petbuddy.petbuddystore.mapper.CategoryMapper;
import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.repository.CategoryRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    CategoryMapper categoryMapper;

    @Override
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        var existedCategory = categoryRepository.findByNameIgnoreCase(request.getName());

        if (existedCategory.isPresent()) {
            Category category = existedCategory.get();

            if (Boolean.TRUE.equals(category.getDeleted())) {
                category.setDeleted(false);
                category.setDeletedAt(null);
                category.setStatus(true);
                category.setDescription(request.getDescription());

                return categoryMapper.toCategoryResponse(categoryRepository.save(category));
            }

            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        Category category = categoryMapper.toCategory(request);
        category.setStatus(true);
        category.setDeleted(false);
        category.setDeletedAt(null);

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByDeletedFalse()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByStatusTrueAndDeletedFalse()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> getAllCategoriesForManagement() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) {
        Category category = getCategoryEntityByIdAndNotDeleted(categoryId);

        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = getCategoryEntityByIdAndNotDeleted(categoryId);

        var existedCategory = categoryRepository.findByNameIgnoreCase(request.getName());

        if (existedCategory.isPresent()
                && !existedCategory.get().getCategoryId().equals(categoryId)
                && Boolean.FALSE.equals(existedCategory.get().getDeleted())) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        categoryMapper.updateCategory(category, request);

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategoryStatus(Long categoryId, Boolean status) {
        Category category = getCategoryEntityByIdAndNotDeleted(categoryId);

        category.setStatus(status);

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public void softDeleteCategory(Long categoryId) {
        Category category = getCategoryEntityByIdAndNotDeleted(categoryId);

        if (productRepository.existsByCategory_CategoryId(categoryId)) {
            throw new AppException(ErrorCode.CATEGORY_HAS_PRODUCTS);
        }

        category.setDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        category.setStatus(false);

        categoryRepository.save(category);
    }
    @Override
    public CategoryResponse restoreCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (Boolean.FALSE.equals(category.getDeleted())) {
            throw new AppException(ErrorCode.CATEGORY_NOT_DELETED);
        }

        category.setDeleted(false);
        category.setDeletedAt(null);
        category.setStatus(true);

        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    private Category getCategoryEntityByIdAndNotDeleted(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (Boolean.TRUE.equals(category.getDeleted())) {
            throw new AppException(ErrorCode.CATEGORY_DELETED);
        }

        return category;
    }

    @Override
    public Category getActiveCategoryEntityById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (Boolean.TRUE.equals(category.getDeleted())) {
            throw new AppException(ErrorCode.CATEGORY_DELETED);
        }

        if (Boolean.FALSE.equals(category.getStatus())) {
            throw new AppException(ErrorCode.CATEGORY_INACTIVE);
        }

        return category;
    }

    @Override
    public Category getActiveCategoryEntityByName(String categoryName) {
        Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (Boolean.TRUE.equals(category.getDeleted())) {
            throw new AppException(ErrorCode.CATEGORY_DELETED);
        }

        if (Boolean.FALSE.equals(category.getStatus())) {
            throw new AppException(ErrorCode.CATEGORY_INACTIVE);
        }

        return category;
    }
}