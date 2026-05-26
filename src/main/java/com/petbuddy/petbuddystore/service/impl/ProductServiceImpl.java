package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductResponse;
import com.petbuddy.petbuddystore.mapper.ProductMapper;
import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.CategoryRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {
        var existedProduct = productRepository.findByNameIgnoreCase(request.getName());

        if (existedProduct.isPresent()) {
            Product product = existedProduct.get();

            if (Boolean.TRUE.equals(product.getDeleted())) {
                Category category = getActiveCategoryById(request.getCategoryId());

                productMapper.updateProduct(product, ProductUpdateRequest.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .stockQuantity(request.getStockQuantity())
                        .imageUrl(request.getImageUrl())
                        .brandName(request.getBrandName())
                        .categoryId(request.getCategoryId())
                        .build());

                product.setCategory(category);
                product.setDeleted(false);
                product.setDeletedAt(null);
                product.setStatus(true);

                return productMapper.toProductResponse(productRepository.save(product));
            }

            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        Category category = getActiveCategoryById(request.getCategoryId());

        Product product = productMapper.toProduct(request);
        product.setCategory(category);
        product.setStatus(true);
        product.setDeleted(false);
        product.setDeletedAt(null);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public Page<ProductResponse> getAllProducts(String keyword, Pageable pageable) {

        if (keyword != null && !keyword.isBlank()) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndDeletedFalse(keyword, pageable)
                    .map(productMapper::toProductResponse);
        }

        return productRepository.findByDeletedFalse(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    public Page<ProductResponse> getActiveProducts(String keyword, Pageable pageable) {

        if (keyword != null && !keyword.isBlank()) {
            return productRepository
                    .findByNameContainingIgnoreCaseAndStatusTrueAndDeletedFalse(keyword, pageable)
                    .map(productMapper::toProductResponse);
        }

        return productRepository.findByStatusTrueAndDeletedFalse(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    public Page<ProductResponse> getAllProductsForManagement(String keyword, Pageable pageable) {

        if (keyword != null && !keyword.isBlank()) {
            return productRepository
                    .findByNameContainingIgnoreCase(keyword, pageable)
                    .map(productMapper::toProductResponse);
        }

        return productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(
            Long categoryId,
            String keyword,
            Pageable pageable
    ) {

        if (keyword != null && !keyword.isBlank()) {
            return productRepository
                    .findByCategory_CategoryIdAndNameContainingIgnoreCaseAndStatusTrueAndDeletedFalse(
                            categoryId,
                            keyword,
                            pageable
                    )
                    .map(productMapper::toProductResponse);
        }

        return productRepository
                .findByCategory_CategoryIdAndStatusTrueAndDeletedFalse(categoryId, pageable)
                .map(productMapper::toProductResponse);
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        Product product = getProductEntityByIdAndNotDeleted(productId);

        return productMapper.toProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = getProductEntityByIdAndNotDeleted(productId);

        var existedProduct = productRepository.findByNameIgnoreCase(request.getName());

        if (existedProduct.isPresent()
                && !existedProduct.get().getProductId().equals(productId)
                && Boolean.FALSE.equals(existedProduct.get().getDeleted())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        Category category = getActiveCategoryById(request.getCategoryId());

        productMapper.updateProduct(product, request);
        product.setCategory(category);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProductStatus(Long productId, Boolean status) {
        Product product = getProductEntityByIdAndNotDeleted(productId);

        product.setStatus(status);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public void softDeleteProduct(Long productId) {
        Product product = getProductEntityByIdAndNotDeleted(productId);

        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(false);

        productRepository.save(product);
    }

    @Override
    public ProductResponse restoreProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (Boolean.FALSE.equals(product.getDeleted())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_DELETED);
        }

        product.setDeleted(false);
        product.setDeletedAt(null);
        product.setStatus(true);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    private Product getProductEntityByIdAndNotDeleted(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            throw new AppException(ErrorCode.PRODUCT_DELETED);
        }

        return product;
    }

    private Category getActiveCategoryById(Long categoryId) {
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
}