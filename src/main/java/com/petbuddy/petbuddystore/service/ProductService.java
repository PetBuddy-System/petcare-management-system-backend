package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductPublicResponse;
import com.petbuddy.petbuddystore.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(ProductCreationRequest request);

    Page<ProductResponse> getAllProducts(String keyword, Pageable pageable);

    Page<ProductPublicResponse> getActiveProducts(Long categoryId, String keyword, String sortBy, Pageable pageable);

    Page<ProductResponse> getAllProductsForManagement(String keyword, Long categoryId, Boolean status, Boolean deleted, Pageable pageable);

    ProductResponse getProductById(UUID productId);

    ProductResponse updateProduct(UUID productId, ProductUpdateRequest request);

    ProductResponse updateProductStatus(UUID productId, Boolean status);

    void softDeleteProduct(UUID productId);

    ProductResponse restoreProduct(UUID productId);

    void importProducts(MultipartFile file);
}