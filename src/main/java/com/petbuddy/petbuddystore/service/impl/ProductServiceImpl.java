package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.ProductStatus;
import com.petbuddy.petbuddystore.common.enums.PromotionStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductDetailResponse;
import com.petbuddy.petbuddystore.dto.response.ProductManagementResponse;
import com.petbuddy.petbuddystore.dto.response.ProductPromotionResponse;
import com.petbuddy.petbuddystore.dto.response.ProductPublicResponse;
import com.petbuddy.petbuddystore.mapper.ProductMapper;
import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.model.ProductBatch;
import com.petbuddy.petbuddystore.model.PromotionDetail;
import com.petbuddy.petbuddystore.repository.ProductBatchRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.repository.PromotionDetailRepository;
import com.petbuddy.petbuddystore.service.CategoryService;
import com.petbuddy.petbuddystore.service.FileService;
import com.petbuddy.petbuddystore.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    ProductBatchRepository productBatchRepository;
    PromotionDetailRepository promotionDetailRepository;
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
        return productMapper.toManagementResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductManagementResponse updateProduct(UUID productId, ProductUpdateRequest request, List<MultipartFile> images) {
        Product product = getProductEntityById(productId);
        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = normalizeName(request.getName());
            productRepository.findByNameIgnoreCaseAndStatusNot(newName, ProductStatus.DELETED)
                    .filter(existed -> !existed.getProductId().equals(productId))
                    .ifPresent(existed -> { throw new AppException(ErrorCode.PRODUCT_EXISTED); });
            request.setName(newName);
        }
        productMapper.updateProduct(product, request);

        if (request.getCategoryId() != null) {
            product.setCategory(categoryService.getActiveCategoryEntityById(request.getCategoryId()));
        }
        if (request.getStatus() != null) {
            updateStatus(product, request.getStatus());
        }
        uploadProductImages(product, images);
        return productMapper.toManagementResponse(productRepository.save(product));
    }

    @Override
    public Product getProductEntityById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Product getActiveProductEntityById(UUID productId) {
        Product product = getProductEntityById(productId);
        if (product.getStatus() == ProductStatus.DELETED) {
            throw new AppException(ErrorCode.PRODUCT_DELETED);
        }
        if (product.getStatus() == ProductStatus.INACTIVE) {
            throw new AppException(ErrorCode.PRODUCT_INACTIVE);
        }
        return product;
    }

    @Override
    public Product getProductEntityByName(String name) {
        return productRepository.findByNameIgnoreCaseAndStatusNot(normalizeName(name), ProductStatus.DELETED)
                .orElse(null);
    }

    @Override
    @Transactional
    public Product createProductFromImport(String name, String description, BigDecimal price, String brandName, Category category, String ingredients, String usageInstructions, List<String> imageUrls) {
        Product product = Product.builder()
                .name(name.trim())
                .description(description)
                .price(price)
                .brandName(brandName)
                .category(category)
                .ingredients(ingredients)
                .usageInstructions(usageInstructions)
                .productCode(generateProductCode())
                .status(ProductStatus.ACTIVE)
                .imageUrls(imageUrls)
                .build();
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateLastBatchSequence(Product product, long lastBatchSequence) {
        product.setLastBatchSequence(lastBatchSequence);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductPublicResponse> getProductsForUser(String keyword, Long categoryId, String brandName, String sortBy, Pageable pageable) {
        return productRepository.findAll(buildProductSpec(keyword, categoryId, brandName, ProductStatus.ACTIVE),
                        buildPageable(pageable, sortBy, "date_desc", "price_asc", "price_desc", "date_asc", "date_desc"))
                .map(product -> {
                    ProductPublicResponse response = productMapper.toPublicResponse(product);
                    response.setTotalStock(getTotalStock(product));
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductManagementResponse> getProductsForManagement(String keyword, Long categoryId, String brandName, ProductStatus status, String sortBy, Pageable pageable) {
        return productRepository.findAll(buildProductSpec(keyword, categoryId, brandName, status),
                        buildPageable(pageable, sortBy, "date_desc", "price_asc", "price_desc", "date_asc", "date_desc"))
                .map(product -> {
                    ProductManagementResponse response = productMapper.toManagementResponse(product);
                    response.setTotalStock(getTotalStock(product));
                    response.setBatchCount(getBatchCount(product));
                    return response;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(UUID productId) {
        Product product = getProductEntityById(productId);
        ProductDetailResponse response = productMapper.toDetailResponse(product);
        Optional.ofNullable(product.getCategory()).ifPresent(cat -> {
            response.setCategoryId(cat.getCategoryId());
            response.setCategoryName(cat.getName());
        });
        response.setTotalStock(getTotalStock(product));
        response.setBatchCount(getBatchCount(product));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductPromotionResponse> getProductsForPromotion(String keyword, Long categoryId, String brandName, Integer nearExpiredDays, String sortBy, Pageable pageable) {
        List<Product> products = productRepository.findAll(buildProductSpec(keyword, categoryId, brandName, ProductStatus.ACTIVE, nearExpiredDays));

        if (products.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<UUID> productIds = products.stream().map(Product::getProductId).toList();

        Map<UUID, List<ProductBatch>> batchesByProduct = productBatchRepository
                .findByProduct_ProductIdInAndStatusAndDeletedAtIsNull(productIds, ProductStatus.ACTIVE)
                .stream()
                .collect(Collectors.groupingBy(b -> b.getProduct().getProductId()));

        Set<UUID> activePromotionProductIds = getActivePromotionProductIds(promotionDetailRepository.findByProduct_ProductIdInAndPromotion_Status(productIds, PromotionStatus.ACTIVE));

        LocalDate expiryThreshold = nearExpiredDays != null ? LocalDate.now().plusDays(nearExpiredDays) : null;

        List<ProductPromotionResponse> responses = products.stream()
                .map(product -> {
                    ProductPromotionResponse response = productMapper.toPromotionResponse(product);
                    populateBatchStats(response, batchesByProduct.getOrDefault(product.getProductId(), Collections.emptyList()), expiryThreshold);
                    response.setHasActivePromotion(activePromotionProductIds.contains(product.getProductId()));
                    return response;
                })
                .sorted(getComparator(sortBy))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());

        if (start >= responses.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, responses.size());
        }
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    private Pageable buildPageable(Pageable pageable, String sortBy, String defaultSort, String... allowedSorts) {
        if (sortBy == null) sortBy = defaultSort;

        Set<String> allowed = new HashSet<>(Arrays.asList(allowedSorts));
        if (!allowed.contains(sortBy)) {
            throw new AppException(ErrorCode.INVALID_SORT_OPTION);
        }

        String field = switch (sortBy) {
            case "price_asc", "price_desc" -> "price";
            case "date_asc", "date_desc" -> "createdAt";
            case "name_asc", "name_desc" -> "name";
            default -> throw new AppException(ErrorCode.INVALID_SORT_OPTION);
        };
        Sort.Direction direction = sortBy.endsWith("_desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, field));
    }

    private Comparator<ProductPromotionResponse> getComparator(String sortBy) {
        if (sortBy == null) return Comparator.comparing(ProductPromotionResponse::getName);

        return switch (sortBy) {
            case "name_asc" -> Comparator.comparing(ProductPromotionResponse::getName);
            case "name_desc" -> Comparator.comparing(ProductPromotionResponse::getName).reversed();
            case "price_asc" -> Comparator.comparing(ProductPromotionResponse::getPrice);
            case "price_desc" -> Comparator.comparing(ProductPromotionResponse::getPrice).reversed();
            case "nearExpiredStock_asc" -> Comparator.comparing(r -> r.getNearExpiredStock() != null ? r.getNearExpiredStock() : 0L);
            case "nearExpiredStock_desc" -> Comparator.comparing((ProductPromotionResponse r) -> r.getNearExpiredStock() != null ? r.getNearExpiredStock() : 0L).reversed();
            case "totalStock_asc" -> Comparator.comparing(r -> r.getTotalStock() != null ? r.getTotalStock() : 0L);
            case "totalStock_desc" -> Comparator.comparing((ProductPromotionResponse r) -> r.getTotalStock() != null ? r.getTotalStock() : 0L).reversed();
            default -> throw new AppException(ErrorCode.INVALID_SORT_OPTION);
        };
    }

    private Set<UUID> getActivePromotionProductIds(List<PromotionDetail> promotionDetails) {
        LocalDateTime now = LocalDateTime.now();
        return promotionDetails.stream()
                .filter(pd -> {
                    var promo = pd.getPromotion();
                    if (promo == null) return false;
                    return (promo.getStartDate() == null || !promo.getStartDate().isAfter(now)) && (promo.getEndDate() == null || !promo.getEndDate().isBefore(now));
                })
                .map(pd -> pd.getProduct().getProductId())
                .collect(Collectors.toSet());
    }

    private void populateBatchStats(ProductPromotionResponse response, List<ProductBatch> batches, LocalDate expiryThreshold) {
        long totalStock = 0;
        long nearExpiredStock = 0;
        long nearExpiredBatchCount = 0;
        LocalDate nearestExpiryDate = null;

        for (ProductBatch batch : batches) {
            int qty = batch.getStockQuantity() == null ? 0 : batch.getStockQuantity();
            totalStock += qty;

            LocalDate expiry = batch.getExpiryDate();
            if (expiry != null) {
                if (nearestExpiryDate == null || expiry.isBefore(nearestExpiryDate)) {
                    nearestExpiryDate = expiry;
                }
                if (expiryThreshold != null && !expiry.isAfter(expiryThreshold)) {
                    nearExpiredStock += qty;
                    nearExpiredBatchCount++;
                }
            }
        }
        response.setTotalStock(totalStock);
        response.setNearExpiredStock(nearExpiredStock);
        response.setNearExpiredBatchCount(nearExpiredBatchCount);
        response.setNearestExpiryDate(nearestExpiryDate);
    }

    private void updateStatus(Product product, ProductStatus status) {
        if (status == ProductStatus.DELETED) {
            boolean hasActiveBatches = productBatchRepository.existsByProduct_ProductIdAndStatusIn(
                    product.getProductId(), List.of(ProductStatus.ACTIVE, ProductStatus.INACTIVE));
            if (hasActiveBatches) {
                throw new AppException(ErrorCode.PRODUCT_HAS_BATCHES);
            }
            product.setDeletedAt(LocalDateTime.now());
        } else {
            product.setDeletedAt(null);
        }
        product.setStatus(status);
    }

    private String generateProductCode() {
        String code;
        do {
            code = "PRD" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        } while (productRepository.existsByProductCode(code));
        return code;
    }

    private Specification<Product> buildProductSpec(String keyword, Long categoryId, String brandName, ProductStatus status) {
        return buildProductSpec(keyword, categoryId, brandName, status, null);
    }

    private Specification<Product> buildProductSpec(String keyword, Long categoryId, String brandName, ProductStatus status, Integer nearExpiredDays) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }
            if (brandName != null && !brandName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("brandName")), "%" + brandName.trim().toLowerCase() + "%"));
            }

            predicates.add(status != null ? cb.equal(root.get("status"), status) : cb.notEqual(root.get("status"), ProductStatus.DELETED));

            if (nearExpiredDays != null) {
                LocalDate threshold = LocalDate.now().plusDays(nearExpiredDays);
                var subquery = query.subquery(Long.class);
                var batchRoot = subquery.from(ProductBatch.class);
                subquery.select(cb.literal(1L));
                subquery.where(
                        cb.equal(batchRoot.get("product"), root),
                        cb.equal(batchRoot.get("status"), ProductStatus.ACTIVE),
                        cb.isNull(batchRoot.get("deletedAt")),
                        cb.greaterThan(batchRoot.get("stockQuantity"), 0),
                        cb.isNotNull(batchRoot.get("expiryDate")),
                        cb.lessThanOrEqualTo(batchRoot.get("expiryDate"), threshold)
                );
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Integer getTotalStock(Product product) {
        if (product.getBatches() == null) return 0;
        return product.getBatches().stream()
                .filter(b -> b.getStatus() == ProductStatus.ACTIVE)
                .mapToInt(b -> b.getStockQuantity() != null ? b.getStockQuantity() : 0)
                .sum();
    }

    private Integer getBatchCount(Product product) {
        if (product.getBatches() == null) return 0;
        return (int) product.getBatches().stream()
                .filter(b -> b.getStatus() != ProductStatus.DELETED)
                .count();
    }

    private void uploadProductImages(Product product, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        if (images.size() > 4) {
            throw new AppException(ErrorCode.PRODUCT_IMAGE_LIMIT_EXCEEDED);
        }
        List<String> imageUrls = images.stream()
                .filter(img -> img != null && !img.isEmpty())
                .map(fileService::uploadProductImage)
                .collect(Collectors.toList());
        if (!imageUrls.isEmpty()) {
            product.setImageUrls(imageUrls);
        }
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}