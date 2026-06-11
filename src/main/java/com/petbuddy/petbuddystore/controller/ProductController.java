package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.dto.response.ProductPublicResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.petbuddy.petbuddystore.common.response.ApiResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductResponse;
import com.petbuddy.petbuddystore.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Product API", description = "Quản lý sản phẩm")
public class ProductController {

    // ==========================================================
    // Pagination Notes For Frontend
    // ==========================================================
    // - default-page-size = 10
    // - max-page-size = 100
    //
    // Nếu FE không truyền size:
    // -> mặc định lấy 10 items/trang
    //
    // Nếu FE truyền size > 100:
    // -> backend tự giới hạn còn 100
    //
    // Backend config trong application.properties:
    // spring.data.web.pageable.default-page-size=10
    // spring.data.web.pageable.max-page-size=100
    //springdoc.default-flat-param-object=true
    // ==========================================================

    ProductService productService;

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_STAFF')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create product",
            description = "Tạo mới sản phẩm. Tạm thời public để test, sau này chỉ ADMIN/MANAGER/STAFF được dùng."
    )
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @ModelAttribute @Valid ProductCreationRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        request.setImage(image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Product created successfully",
                        productService.createProduct(request)
                ));
    }

    @GetMapping
    @Operation(
            summary = "Get products",
            description = "Lấy danh sách sản phẩm chưa bị xóa mềm, có hỗ trợ search và phân trang."
    )
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        productService.getAllProducts(keyword, pageable)
                )
        );
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active products",
            description = "Lấy danh sách sản phẩm đang hoạt động và chưa bị xóa."
    )
    public ResponseEntity<ApiResponse<Page<ProductPublicResponse>>> getActiveProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(productService.getActiveProducts(categoryId, keyword, sortBy, pageable))
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_STAFF')")
    @GetMapping("/management")
    @Operation(
            summary = "Get products for management",
            description = "Lấy danh sách sản phẩm cho ADMIN/MANAGER/STAFF quản lý."
    )
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProductsForManagement(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Boolean deleted,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getAllProductsForManagement(keyword, categoryId, status, deleted, pageable))
        );
    }


    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product by id",
            description = "Lấy chi tiết sản phẩm theo id.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID productId) {
        return ResponseEntity.ok(
                ApiResponse.success(productService.getProductById(productId))
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_STAFF')")
    @PatchMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update product",
            description = "Cập nhật thông tin sản phẩm.")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID  productId,
            @ModelAttribute ProductUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        request.setImage(image);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product updated successfully",
                        productService.updateProduct(productId, request)
                )
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_STAFF')")
    @PatchMapping("/{productId}/status")
    @Operation(
            summary = "Update product status",
            description = "Bật/tắt trạng thái sản phẩm.")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable UUID  productId,
            @RequestParam Boolean status) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product status updated successfully",
                        productService.updateProductStatus(productId, status)
                )
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    @DeleteMapping("/{productId}/soft-deleted")
    @Operation(
            summary = "Soft delete product",
            description = "Xóa mềm sản phẩm.")
    public ResponseEntity<ApiResponse<Void>> softDeleteProduct(@PathVariable UUID  productId) {
        productService.softDeleteProduct(productId);

        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully")
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER')")
    @PatchMapping("/{productId}/restore")
    @Operation(
            summary = "Restore product",
            description = "Khôi phục sản phẩm đã bị xóa mềm.")
    public ResponseEntity<ApiResponse<ProductResponse>> restoreProduct(@PathVariable UUID  productId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Product restored successfully",
                        productService.restoreProduct(productId)
                )
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_MANAGER','ROLE_STAFF')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> importProducts(
            @RequestPart("file") MultipartFile file
    ) {
        productService.importProducts(file);

        return ResponseEntity.ok(
                ApiResponse.success("Import products successfully")
        );
    }
}