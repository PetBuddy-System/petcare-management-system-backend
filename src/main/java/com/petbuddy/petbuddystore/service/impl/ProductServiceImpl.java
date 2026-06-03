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
import com.petbuddy.petbuddystore.service.FileService;
import com.petbuddy.petbuddystore.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductMapper productMapper;
    FileService fileService;

    @Transactional
    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {

        validateExpiryDate(request.getExpiryDate());
        var existedProduct = productRepository.findByNameIgnoreCase(request.getName());

        if (existedProduct.isPresent()) {
            Product product = existedProduct.get();

            if (Boolean.TRUE.equals(product.getDeleted())) {

                Category category = getActiveCategoryById(request.getCategoryId());

                if (request.getImage() != null && !request.getImage().isEmpty()) {

                    String newImageUrl = fileService.uploadImage(request.getImage());

                    if (product.getImageUrl() != null) {
                        fileService.deleteImage(product.getImageUrl());
                    }

                    product.setImageUrl(newImageUrl);
                }

                product.setName(request.getName());
                product.setDescription(request.getDescription());
                product.setPrice(request.getPrice());
                product.setStockQuantity(request.getStockQuantity());
                product.setBrandName(request.getBrandName());

                product.setCategory(category);
                product.setDeleted(false);
                product.setDeletedAt(null);
                product.setStatus(true);

                return productMapper.toProductResponse(productRepository.save(product));
            }

            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }

        String imageUrl = null;

        try {

            imageUrl = fileService.uploadImage(request.getImage());

            Category category = getActiveCategoryById(request.getCategoryId());

            Product product = productMapper.toProduct(request);

            product.setImageUrl(imageUrl);
            product.setCategory(category);
            product.setStatus(true);
            product.setDeleted(false);
            product.setDeletedAt(null);

            return productMapper.toProductResponse(productRepository.save(product));

        } catch (Exception e) {

            if (imageUrl != null) {
                fileService.deleteImage(imageUrl);
            }

            throw e;
        }
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

    @Transactional
    @Override
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {

        if (request.getExpiryDate() != null) {
            validateExpiryDate(request.getExpiryDate());
        }        Product product = getProductEntityByIdAndNotDeleted(productId);

        if (request.getName() != null && !request.getName().isBlank()) {

            var existedProduct = productRepository.findByNameIgnoreCase(request.getName());

            if (existedProduct.isPresent()
                    && !existedProduct.get().getProductId().equals(productId)
                    && Boolean.FALSE.equals(existedProduct.get().getDeleted())) {

                throw new AppException(ErrorCode.PRODUCT_EXISTED);
            }
        }

        if (request.getCategoryId() != null) {
            Category category = getActiveCategoryById(request.getCategoryId());
            product.setCategory(category);
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {

            String oldImageUrl = product.getImageUrl();

            String newImageUrl = fileService.uploadImage(request.getImage());

            product.setImageUrl(newImageUrl);

            if (oldImageUrl != null) {
                fileService.deleteImage(oldImageUrl);
            }
        }

        productMapper.updateProduct(product, request);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProductStatus(Long productId, Boolean status) {
        if (status == null) {
            throw new AppException(ErrorCode.PRODUCT_STATUS_REQUIRED);
        }

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

    @Transactional
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

    @Override
    @Transactional
    public void importProducts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            validateExcelHeader(sheet.getRow(0));

            Map<Integer, PictureData> picturesByRow = getPicturesByRow(sheet);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    continue;
                }

                String categoryName = getStringCell(row, 5);

                ProductCreationRequest request = ProductCreationRequest.builder()
                        .name(getStringCell(row, 0))
                        .description(getStringCell(row, 1))
                        .price(getBigDecimalCell(row, 2))
                        .stockQuantity(getIntegerCell(row, 3))
                        .brandName(getStringCell(row, 4))
                        .expiryDate(getLocalDateCell(row, 6))
                        .build();

                validateExpiryDate(request.getExpiryDate());

                Category category = getActiveCategoryByName(categoryName);

                var existedProduct = productRepository.findByNameIgnoreCase(request.getName());

                if (existedProduct.isPresent()
                        && Boolean.FALSE.equals(existedProduct.get().getDeleted())) {
                    throw new AppException(ErrorCode.PRODUCT_EXISTED);
                }

                String imageUrl = null;

                PictureData pictureData = picturesByRow.get(i);

                if (pictureData != null) {
                    imageUrl = fileService.uploadImage(
                            pictureData.getData(),
                            request.getName() + "." + pictureData.suggestFileExtension(),
                            pictureData.getMimeType()
                    );
                }

                Product product;

                if (existedProduct.isPresent()) {
                    product = existedProduct.get();

                    product.setDeleted(false);
                    product.setDeletedAt(null);
                    product.setStatus(true);

                    if (product.getImageUrl() != null && imageUrl != null) {
                        fileService.deleteImage(product.getImageUrl());
                    }
                } else {
                    product = productMapper.toProduct(request);

                    product.setStatus(true);
                    product.setDeleted(false);
                    product.setDeletedAt(null);
                }

                product.setName(request.getName());
                product.setDescription(request.getDescription());
                product.setPrice(request.getPrice());
                product.setStockQuantity(request.getStockQuantity());
                product.setBrandName(request.getBrandName());
                product.setExpiryDate(request.getExpiryDate());
                product.setCategory(category);

                if (imageUrl != null) {
                    product.setImageUrl(imageUrl);
                }

                productRepository.save(product);
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.IMPORT_FAILED);
        }
    }

    private String getStringCell(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        return cell.toString().trim();
    }
    private BigDecimal getBigDecimalCell(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        return new BigDecimal(cell.toString().trim());
    }
    private Integer getIntegerCell(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }

        return Integer.parseInt(cell.toString().trim());
    }
    private Long getLongCell(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }

        return Long.parseLong(cell.toString().trim());
    }
    private LocalDate getLocalDateCell(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String value = cell.toString().trim();

        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }

        throw new AppException(ErrorCode.EXPIRY_DATE_INVALID);
    }

    private Map<Integer, PictureData> getPicturesByRow(Sheet sheet) {
        Map<Integer, PictureData> pictureMap = new HashMap<>();

        Drawing<?> drawing = sheet.getDrawingPatriarch();

        if (drawing == null) {
            return pictureMap;
        }

        for (Shape shape : drawing) {
            if (shape instanceof Picture picture) {
                ClientAnchor anchor = picture.getClientAnchor();

                int rowIndex = anchor.getRow1();
                pictureMap.put(rowIndex, picture.getPictureData());
            }
        }

        return pictureMap;
    }
    private void validateExcelHeader(Row header) {
        if (header == null) {
            throw new AppException(ErrorCode.INVALID_EXCEL_TEMPLATE);
        }

        String[] expectedHeaders = {
                "Name",
                "Description",
                "Price",
                "StockQuantity",
                "BrandName",
                "CategoryName",
                "ExpiryDate",
                "Image"
        };

        for (int i = 0; i < expectedHeaders.length; i++) {
            Cell cell = header.getCell(i);

            if (cell == null || !expectedHeaders[i].equalsIgnoreCase(cell.toString().trim())) {
                throw new AppException(ErrorCode.INVALID_EXCEL_TEMPLATE);
            }
        }
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

    private void validateExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            throw new AppException(ErrorCode.EXPIRY_DATE_REQUIRED);
        }

        if (!expiryDate.isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.EXPIRY_DATE_INVALID);
        }
    }

    private Category getActiveCategoryByName(String categoryName) {
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