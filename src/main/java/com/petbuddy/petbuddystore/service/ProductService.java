package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    ProductResponse createProduct(ProductCreationRequest request);

    Page<ProductResponse> getAllProducts(String keyword, Pageable pageable);

    Page<ProductResponse> getActiveProducts(String keyword, Pageable pageable);

    Page<ProductResponse> getAllProductsForManagement(String keyword, Pageable pageable);

    Page<ProductResponse> getProductsByCategory(Long categoryId, String keyword, Pageable pageable);

    ProductResponse getProductById(Long productId);

    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);

    ProductResponse updateProductStatus(Long productId, Boolean status);

    void softDeleteProduct(Long productId);

    ProductResponse restoreProduct(Long productId);

    void importProducts(MultipartFile file);
}