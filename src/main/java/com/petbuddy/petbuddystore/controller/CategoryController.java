package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.CategoryCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CategoryUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CategoryResponse;
import com.petbuddy.petbuddystore.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Category API", description = "Quản lý danh mục sản phẩm")
public class CategoryController {

    CategoryService categoryService;

    @PostMapping("/create")
    @Operation(
            summary = "Create category",
            description = "Tạo mới danh mục sản phẩm. Tạm thời public để test, sau này chỉ ADMIN/MANAGER/STAFF được dùng."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryCreationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created successfully",
                        categoryService.createCategory(request)
                ));
    }

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Lấy toàn bộ danh mục sản phẩm, bao gồm cả active và inactive."
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getAllCategories())
        );
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active categories",
            description = "Lấy danh sách danh mục đang hoạt động để hiển thị cho khách hàng."
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getActiveCategories() {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getActiveCategories())
        );
    }
    @GetMapping("/all-category")
    @Operation(
            summary = "Get all categories for management",
            description = "Lấy toàn bộ danh mục sản phẩm, bao gồm cả active, inactive và deleted, để hiển thị cho quản lý."
    )
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesForManagement() {
        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getAllCategoriesForManagement())
        );
    }


    @GetMapping("/{categoryId}")
    @Operation(
            summary = "Get category by id",
            description = "Lấy chi tiết một danh mục theo categoryId."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                ApiResponse.success(categoryService.getCategoryById(categoryId))
        );
    }

    @PutMapping("/{categoryId}/update")
    @Operation(
            summary = "Update category",
            description = "Cập nhật tên, mô tả và trạng thái của danh mục."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category updated successfully",
                        categoryService.updateCategory(categoryId, request)
                )
        );
    }

    @PatchMapping("/{categoryId}/active")
    @Operation(
            summary = "Activate category",
            description = "Mở lại danh mục đã bị khóa/xóa mềm."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> activateCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category activated successfully",
                        categoryService.updateCategoryStatus(categoryId, true)
                )
        );
    }

    @PatchMapping("/{categoryId}/inactive")
    @Operation(
            summary = "Deactivate category",
            description = "Khóa danh mục, không hiển thị cho khách hàng. Đây là xóa mềm, không xóa khỏi database."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivateCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category deactivated successfully",
                        categoryService.updateCategoryStatus(categoryId, false)
                )
        );
    }

    @DeleteMapping("/{categoryId}/soft-deleted")
    @Operation(
            summary = "Soft delete category",
            description = "Xóa mềm category. Category sẽ không bị xóa khỏi database, chỉ chuyển deleted = true, status = false."
    )
    public ResponseEntity<ApiResponse<Void>> softDeleteCategory(
            @PathVariable Long categoryId) {

        categoryService.softDeleteCategory(categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Category deleted successfully")
        );
    }


    @PatchMapping("/{categoryId}/restore")
    @Operation(
            summary = "Restore deleted category",
            description = "Khôi phục category đã bị xóa mềm. Sau khi restore, deleted = false, status = true."
    )
    public ResponseEntity<ApiResponse<CategoryResponse>> restoreCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category restored successfully",
                        categoryService.restoreCategory(categoryId)
                )
        );
    }
}