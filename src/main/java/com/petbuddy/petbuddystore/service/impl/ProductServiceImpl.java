package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.ProductStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductDetailResponse;
import com.petbuddy.petbuddystore.dto.response.ProductManagementResponse;
import com.petbuddy.petbuddystore.dto.response.ProductPublicResponse;
import com.petbuddy.petbuddystore.mapper.ProductMapper;
import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.ProductBatchRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.CategoryService;
import com.petbuddy.petbuddystore.service.FileService;
import com.petbuddy.petbuddystore.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductBatchRepository productBatchRepository;
    CategoryService categoryService;
    FileService fileService;
    ProductMapper productMapper;

    @Override
    @Transactional
    public ProductManagementResponse createProduct(ProductCreationRequest request, List<MultipartFile> images) {
        String productName = normalizeName(request.getName());
        if (productRepository.existsByNameIgnoreCaseAndStatusNot(productName, ProductStatus.DELETED)) {throw new AppException(ErrorCode.PRODUCT_EXISTED);}
        Category category = categoryService.getActiveCategoryEntityById(request.getCategoryId());
        Product product = productMapper.toProduct(request);
        product.setProductCode(generateProductCode());
        product.setCategory(category);
        uploadProductImages(product, images);
        Product savedProduct = productRepository.save(product);
        return productMapper.toManagementResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductPublicResponse> getProductsForUser(String keyword, Long categoryId, String brandName, String sortBy, Pageable pageable) {
        Pageable sortedPageable = buildPageable(pageable, sortBy);
        Specification<Product> spec = buildProductSpec(keyword, categoryId, brandName, ProductStatus.ACTIVE);
        return productRepository.findAll(spec, sortedPageable)
                .map(product -> {ProductPublicResponse response = productMapper.toPublicResponse(product);
                    response.setTotalStock(getTotalStock(product));
                    return response;});
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductManagementResponse> getProductsForManagement(String keyword, Long categoryId, String brandName, ProductStatus status, String sortBy, Pageable pageable) {
        Pageable sortedPageable = buildPageable(pageable, sortBy);
        Specification<Product> spec = buildProductSpec(keyword, categoryId, brandName, status);
        return productRepository.findAll(spec, sortedPageable)
                .map(product -> {ProductManagementResponse response = productMapper.toManagementResponse(product);
                    response.setTotalStock( getTotalStock(product));
                    response.setBatchCount(getBatchCount(product));
                    return response;});
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(UUID productId) {Product product = getProductEntityById(productId);
        ProductDetailResponse response = productMapper.toDetailResponse(product);
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getCategoryId());
            response.setCategoryName(product.getCategory().getName());
        }
        response.setTotalStock(getTotalStock(product));
        response.setBatchCount(getBatchCount(product));
        return response;
    }

    @Override
    @Transactional
    public ProductManagementResponse updateProduct(UUID productId, ProductUpdateRequest request, List<MultipartFile> images) {
        Product product = getProductEntityById(productId);
        List<MultipartFile> safeImages = images == null ? null : new ArrayList<>(images);
        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = normalizeName(request.getName());
            productRepository.findByNameIgnoreCaseAndStatusNot(newName, ProductStatus.DELETED)
                    .filter(existedProduct -> !existedProduct.getProductId().equals(productId))
                    .ifPresent(existedProduct -> {throw new AppException(ErrorCode.PRODUCT_EXISTED);});
            request.setName(newName);
        }
        productMapper.updateProduct(product, request);
        if (request.getCategoryId() != null) {Category category = categoryService.getActiveCategoryEntityById(request.getCategoryId());product.setCategory(category);}
        if (request.getStatus() != null) {updateStatus(product, request.getStatus());}
        uploadProductImages(product, safeImages);
        Product savedProduct = productRepository.save(product);
        return productMapper.toManagementResponse(savedProduct);
    }

    @Override
    public Product getProductEntityById(UUID productId) {
        return productRepository.findById(productId).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Product getActiveProductEntityById(UUID productId) {
        Product product = getProductEntityById(productId);
        if (product.getStatus() == ProductStatus.DELETED) {throw new AppException(ErrorCode.PRODUCT_DELETED);}
        if (product.getStatus() == ProductStatus.INACTIVE) {throw new AppException(ErrorCode.PRODUCT_INACTIVE);}
        return product;
    }

    @Override
    public Product getProductEntityByName(String name) {return productRepository.findByNameIgnoreCaseAndStatusNot(normalizeName(name), ProductStatus.DELETED).orElse(null);}

    @Override
    @Transactional
    public Product createProductFromImport(String name, String description, BigDecimal price, String brandName, Category category, String ingredients, String usageInstructions, List<String> imageUrls) {
        Product.ProductBuilder builder = Product.builder()
                .name(name.trim())
                .description(description)
                .price(price)
                .brandName(brandName)
                .category(category)
                .ingredients(ingredients)
                .usageInstructions(usageInstructions)
                .productCode(generateProductCode())
                .status(ProductStatus.ACTIVE);
        if (imageUrls != null && !imageUrls.isEmpty()) {builder.imageUrls(imageUrls);}
        return productRepository.save(builder.build());
    }

    @Override
    @Transactional
    public void updateLastBatchSequence(Product product, long lastBatchSequence) {
        product.setLastBatchSequence(lastBatchSequence);
        productRepository.save(product);
    }

    private void updateStatus(Product product, ProductStatus status) {

        if (status == ProductStatus.DELETED && productBatchRepository.existsByProduct_ProductIdAndStatusIn(
                product.getProductId(),
                List.of(ProductStatus.ACTIVE, ProductStatus.INACTIVE)
        )) {throw new AppException(ErrorCode.PRODUCT_HAS_BATCHES);}
        product.setStatus(status);
        if (status == ProductStatus.DELETED) {product.setDeletedAt(LocalDateTime.now());} else {product.setDeletedAt(null);}
    }

    private String generateProductCode() {
        String code;
        do {
            code = "PRD" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (productRepository.existsByProductCode(code));
        return code;
    }

    private Pageable buildPageable(Pageable pageable, String sortBy) {
        Sort sort = switch (sortBy == null ? "date_desc" : sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> throw new AppException(ErrorCode.INVALID_SORT_OPTION);
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Specification<Product> buildProductSpec(String keyword, Long categoryId, String brandName, ProductStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));}
            if (categoryId != null) {predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));}
            if (brandName != null && !brandName.isBlank()) {predicates.add(cb.like(cb.lower(root.get("brandName")), "%" + brandName.trim().toLowerCase() + "%"));}
            if (status != null) {predicates.add(cb.equal(root.get("status"), status));
            } else {predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));}
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Integer getTotalStock(Product product) {
        if (product.getBatches() == null) {return 0;}
        return product.getBatches()
                .stream().filter(batch -> batch.getStatus() == ProductStatus.ACTIVE)
                .mapToInt(batch -> batch.getStockQuantity() == null ? 0 : batch.getStockQuantity()).sum();
    }

    private Integer getBatchCount(Product product) {if (product.getBatches() == null) {return 0;}
        return (int) product.getBatches().stream().filter(batch -> batch.getStatus() != ProductStatus.DELETED).count();
    }

    private void uploadProductImages(Product product, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {return;}
        if (images.size() > 4) {throw new AppException(ErrorCode.PRODUCT_IMAGE_LIMIT_EXCEEDED);}
        List<String> imageUrls = images.stream().filter(image -> image != null && !image.isEmpty()).map(fileService::uploadProductImage).toList();
        if (!imageUrls.isEmpty()) {product.setImageUrls(imageUrls);}
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}