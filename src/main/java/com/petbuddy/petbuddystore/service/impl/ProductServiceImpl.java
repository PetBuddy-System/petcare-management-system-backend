package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductPublicResponse;
import com.petbuddy.petbuddystore.dto.response.ProductResponse;
import com.petbuddy.petbuddystore.mapper.ProductMapper;
import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import com.petbuddy.petbuddystore.service.CategoryService;
import com.petbuddy.petbuddystore.service.FileService;
import com.petbuddy.petbuddystore.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryService categoryService;
    FileService fileService;
    ProductMapper productMapper;

    @Transactional
    @Override
    public ProductResponse createProduct(ProductCreationRequest request) {
        validateExpiryDate(request.getExpiryDate());

        Category category = categoryService.getActiveCategoryEntityById(request.getCategoryId());

        Product product = productMapper.toProduct(request);
        product.setProductCode(generateProductCode());
        activateProduct(product);

        Optional<Product> oldProductWithImage =
                productRepository.findFirstByNameIgnoreCaseAndImageUrlIsNotNull(request.getName());

        oldProductWithImage.ifPresent(oldProduct ->
                product.setImageUrl(oldProduct.getImageUrl())
        );

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            product.setImageUrl(fileService.uploadProductImage(request.getImage()));
        }

        applyProductInfo(product, request, category);

        Product savedProduct = productRepository.save(product);

        syncSharedFieldsAfterCreateOrImport(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getBrandName(),
                category,
                savedProduct.getImageUrl()
        );

        return productMapper.toProductResponse(savedProduct);
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
    public Page<ProductPublicResponse> getActiveProducts(Long categoryId, String keyword, String sortBy, Pageable pageable) {
        List<Product> products;

        if (categoryId != null) {
            if (keyword != null && !keyword.isBlank()) {
                products = productRepository
                        .findByCategory_CategoryIdAndNameContainingIgnoreCaseAndStatusTrueAndDeletedFalse(
                                categoryId,
                                keyword,
                                Pageable.unpaged()
                        )
                        .getContent();
            } else {
                products = productRepository
                        .findByCategory_CategoryIdAndStatusTrueAndDeletedFalse(
                                categoryId,
                                Pageable.unpaged()
                        )
                        .getContent();
            }
        } else {
            if (keyword != null && !keyword.isBlank()) {
                products = productRepository
                        .findByNameContainingIgnoreCaseAndStatusTrueAndDeletedFalse(
                                keyword,
                                Pageable.unpaged()
                        )
                        .getContent();
            } else {
                products = productRepository
                        .findByStatusTrueAndDeletedFalse(Pageable.unpaged())
                        .getContent();
            }
        }

        return groupProductsForUser(products, sortBy, pageable);
    }

    @Override
    public Page<ProductResponse> getAllProductsForManagement(String keyword, Long categoryId, Boolean status, Boolean deleted, Pageable pageable) {
        List<Product> products = productRepository.findAll(Pageable.unpaged()).getContent();

        List<Product> filteredProducts = products.stream()
                .filter(product -> keyword == null
                        || keyword.isBlank()
                        || product.getName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(product -> categoryId == null
                        || (product.getCategory() != null
                        && Objects.equals(product.getCategory().getCategoryId(), categoryId)))
                .filter(product -> status == null
                        || Objects.equals(product.getStatus(), status))
                .filter(product -> deleted == null
                        || Objects.equals(product.getDeleted(), deleted))
                .sorted(Comparator.comparing(Product::getCreatedAt).reversed())
                .toList();

        List<ProductResponse> responses = filteredProducts.stream()
                .map(productMapper::toProductResponse)
                .toList();

        return paginate(responses, pageable);
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        Product product = getProductEntityByIdAndNotDeleted(productId);
        return productMapper.toProductResponse(product);
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
        Product selectedProduct = getProductEntityByIdAndNotDeleted(productId);

        if (request.getExpiryDate() != null) {
            validateExpiryDate(request.getExpiryDate());
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getActiveCategoryEntityById(request.getCategoryId());
        }

        String newImageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            newImageUrl = fileService.uploadProductImage(request.getImage());
        }

        List<Product> sameNameProducts =
                productRepository.findByNameIgnoreCase(selectedProduct.getName())
                        .stream()
                        .filter(product -> Boolean.FALSE.equals(product.getDeleted()))
                        .toList();

        updateSharedFields(sameNameProducts, request, category, newImageUrl);
        updateBatchFields(selectedProduct, request);

        productRepository.saveAll(sameNameProducts);

        return productMapper.toProductResponse(selectedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(UUID productId, Boolean status) {
        if (status == null) {
            throw new AppException(ErrorCode.PRODUCT_STATUS_REQUIRED);
        }

        Product selectedProduct = getProductEntityByIdAndNotDeleted(productId);

        List<Product> sameNameProducts =
                productRepository.findByNameIgnoreCase(selectedProduct.getName())
                        .stream()
                        .filter(product -> Boolean.FALSE.equals(product.getDeleted()))
                        .toList();

        for (Product product : sameNameProducts) {
            product.setStatus(status);
        }

        productRepository.saveAll(sameNameProducts);

        selectedProduct.setStatus(status);
        return productMapper.toProductResponse(selectedProduct);
    }

    @Override
    public void softDeleteProduct(UUID productId) {
        Product product = getProductEntityByIdAndNotDeleted(productId);
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(false);

        productRepository.save(product);
    }

    @Transactional
    @Override
    public ProductResponse restoreProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (Boolean.FALSE.equals(product.getDeleted())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_DELETED);
        }

        activateProduct(product);

        return productMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void importProducts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        try {
            byte[] excelBytes = file.getBytes();

            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes))) {
                Sheet sheet = workbook.getSheetAt(0);

                validateExcelHeader(sheet.getRow(0));

                Map<Integer, ExcelImageData> picturesByRow = getImagesByRow(excelBytes, sheet);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);

                    if (row == null || isEmptyRow(row)) {
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

                    Category category = categoryService.getActiveCategoryEntityByName(categoryName);

                    String imageUrl = null;

                    ExcelImageData pictureData = picturesByRow.get(i);

                    if (pictureData != null) {
                        imageUrl = fileService.uploadImage(
                                pictureData.data,
                                request.getName() + "." + pictureData.extension,
                                pictureData.contentType
                        );
                    }

                    if (imageUrl == null) {
                        imageUrl = productRepository
                                .findFirstByNameIgnoreCaseAndImageUrlIsNotNull(request.getName())
                                .map(Product::getImageUrl)
                                .orElse(null);
                    }

                    Product product = productMapper.toProduct(request);
                    product.setProductCode(generateProductCode());
                    activateProduct(product);

                    applyProductInfo(product, request, category);

                    if (product.getImageUrl() == null && imageUrl != null) {
                        product.setImageUrl(imageUrl);
                    }

                    Product savedProduct = productRepository.save(product);

                    syncSharedFieldsAfterCreateOrImport(
                            request.getName(),
                            request.getDescription(),
                            request.getPrice(),
                            request.getBrandName(),
                            category,
                            savedProduct.getImageUrl()
                    );
                }
            }

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Import products failed", e);
            throw new AppException(ErrorCode.IMPORT_FAILED);
        }
    }

    private void updateSharedFields(List<Product> products, ProductUpdateRequest request, Category category, String imageUrl) {
        for (Product product : products) {
            if (request.getPrice() != null) product.setPrice(request.getPrice());
            if (request.getBrandName() != null) product.setBrandName(request.getBrandName());
            if (request.getDescription() != null) product.setDescription(request.getDescription());
            if (category != null) product.setCategory(category);
            if (imageUrl != null) product.setImageUrl(imageUrl);
        }
    }

    private void updateBatchFields(Product product, ProductUpdateRequest request) {
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }

        if (request.getExpiryDate() != null) {
            product.setExpiryDate(request.getExpiryDate());
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

    private LocalDate getLocalDateCell(Row row, int index) {
        Cell cell = row.getCell(index);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String value = cell.toString().trim();

        if (value.isBlank()) {
            return null;
        }

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

    private static class ExcelImageData {
        byte[] data;
        String extension;
        String contentType;

        ExcelImageData(byte[] data, String extension, String contentType) {
            this.data = data;
            this.extension = extension;
            this.contentType = contentType;
        }
    }

    private Map<Integer, ExcelImageData> getImagesByRow(byte[] excelBytes, Sheet sheet) {
        Map<Integer, ExcelImageData> imageMap = new HashMap<>();

        imageMap.putAll(getDrawingImagesByRow(sheet));
        imageMap.putAll(getRichDataImagesByRow(excelBytes));

        log.info("Total images mapped by row: {}", imageMap.size());

        return imageMap;
    }

    private Map<Integer, ExcelImageData> getDrawingImagesByRow(Sheet sheet) {
        Map<Integer, ExcelImageData> imageMap = new HashMap<>();

        Drawing<?> drawing = sheet.getDrawingPatriarch();

        if (drawing == null) {
            return imageMap;
        }

        for (Shape shape : drawing) {
            if (shape instanceof Picture picture) {
                ClientAnchor anchor = picture.getClientAnchor();
                PictureData data = picture.getPictureData();

                int rowIndex = anchor.getRow1();

                imageMap.put(
                        rowIndex,
                        new ExcelImageData(
                                data.getData(),
                                data.suggestFileExtension(),
                                data.getMimeType()
                        )
                );

                log.info("Found floating image at row {}", rowIndex);
            }
        }

        return imageMap;
    }

    private Map<Integer, ExcelImageData> getRichDataImagesByRow(byte[] excelBytes) {
        Map<Integer, ExcelImageData> imageMap = new HashMap<>();

        try {
            Map<String, byte[]> entries = readZipEntries(excelBytes);

            byte[] sheetXml = entries.get("xl/worksheets/sheet1.xml");
            byte[] relXml = entries.get("xl/richData/_rels/richValueRel.xml.rels");

            if (sheetXml == null || relXml == null) {
                return imageMap;
            }

            Map<String, String> relIdToImagePath = readRichDataRelationships(relXml);

            Document sheetDocument = parseXml(sheetXml);
            NodeList cells = sheetDocument.getElementsByTagName("c");

            for (int i = 0; i < cells.getLength(); i++) {
                Element cell = (Element) cells.item(i);

                String cellRef = cell.getAttribute("r");
                String vm = cell.getAttribute("vm");

                if (cellRef == null || vm == null || vm.isBlank()) {
                    continue;
                }

                if (!cellRef.startsWith("H")) {
                    continue;
                }

                int rowIndex = extractRowIndex(cellRef);
                String relId = "rId" + vm;

                String imagePath = relIdToImagePath.get(relId);

                if (imagePath == null) {
                    continue;
                }

                byte[] imageBytes = entries.get(imagePath);

                if (imageBytes == null) {
                    continue;
                }

                String extension = getExtension(imagePath);
                String contentType = getContentType(extension);

                imageMap.put(
                        rowIndex,
                        new ExcelImageData(imageBytes, extension, contentType)
                );

                log.info("Found richData image at row {}, path={}", rowIndex, imagePath);
            }

        } catch (Exception e) {
            log.warn("Cannot read richData images from Excel: {}", e.getMessage());
        }

        return imageMap;
    }

    private Map<String, byte[]> readZipEntries(byte[] excelBytes) throws Exception {
        Map<String, byte[]> entries = new HashMap<>();

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(new ByteArrayInputStream(excelBytes))) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                entries.put(entry.getName(), zipInputStream.readAllBytes());
            }
        }

        return entries;
    }

    private Map<String, String> readRichDataRelationships(byte[] relXml) throws Exception {
        Map<String, String> result = new HashMap<>();

        Document document = parseXml(relXml);
        NodeList relationships = document.getElementsByTagName("Relationship");

        for (int i = 0; i < relationships.getLength(); i++) {
            Element relationship = (Element) relationships.item(i);

            String id = relationship.getAttribute("Id");
            String target = relationship.getAttribute("Target");

            if (id == null || target == null) {
                continue;
            }

            result.put(id, normalizeImagePath(target));
        }

        return result;
    }

    private Document parseXml(byte[] xmlBytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);

        return factory
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xmlBytes));
    }

    private String normalizeImagePath(String target) {
        while (target.startsWith("../")) {
            target = target.substring(3);
        }

        if (!target.startsWith("xl/")) {
            target = "xl/" + target;
        }

        return target;
    }

    private int extractRowIndex(String cellRef) {
        String rowNumber = cellRef.replaceAll("[^0-9]", "");
        return Integer.parseInt(rowNumber) - 1;
    }

    private String getExtension(String path) {
        int dotIndex = path.lastIndexOf(".");

        if (dotIndex == -1) {
            return "jpg";
        }

        return path.substring(dotIndex + 1).toLowerCase();
    }

    private String getContentType(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
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

    private Product getProductEntityByIdAndNotDeleted(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            throw new AppException(ErrorCode.PRODUCT_DELETED);
        }

        return product;
    }

    private void validateExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            return;
        }

        if (!expiryDate.isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.EXPIRY_DATE_INVALID);
        }
    }

    private void syncSharedFieldsAfterCreateOrImport(
            String name,
            String description,
            BigDecimal price,
            String brandName,
            Category category,
            String imageUrl
    ) {
        List<Product> sameNameProducts =
                productRepository.findByNameIgnoreCase(name)
                        .stream()
                        .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                        .toList();

        for (Product product : sameNameProducts) {
            product.setDescription(description);
            product.setPrice(price);
            product.setBrandName(brandName);
            product.setCategory(category);

            if (imageUrl != null) {
                product.setImageUrl(imageUrl);
            }
        }

        productRepository.saveAll(sameNameProducts);
    }

    private Page<ProductPublicResponse> groupProductsForUser(List<Product> products, String sortBy, Pageable pageable) {
        List<List<Product>> groupedProductBatches = products.stream()
                .collect(Collectors.groupingBy(Product::getName))
                .values()
                .stream()
                .sorted((group1, group2) -> compareProductGroups(group1, group2, sortBy))
                .toList();

        List<ProductPublicResponse> groupedProducts = groupedProductBatches.stream()
                .map(group -> {
                    Product latestProduct = getLatestProduct(group);

                    int totalStock = group.stream()
                            .mapToInt(product -> product.getStockQuantity() == null ? 0 : product.getStockQuantity())
                            .sum();

                    ProductPublicResponse response = productMapper.toPublicResponse(latestProduct);
                    response.setTotalStock(totalStock);

                    return response;
                })
                .toList();

        return paginate(groupedProducts, pageable);
    }

    private int compareProductGroups(List<Product> group1, List<Product> group2, String sortBy
    ) {
        Product product1 = getLatestProduct(group1);
        Product product2 = getLatestProduct(group2);

        if ("priceAsc".equalsIgnoreCase(sortBy)) {
            return product1.getPrice().compareTo(product2.getPrice());
        }

        if ("priceDesc".equalsIgnoreCase(sortBy)) {
            return product2.getPrice().compareTo(product1.getPrice());
        }

        return product2.getCreatedAt().compareTo(product1.getCreatedAt());
    }

    private Product getLatestProduct(List<Product> products) {
        return products.stream()
                .max(Comparator.comparing(Product::getCreatedAt))
                .orElseThrow();
    }

    private <T> Page<T> paginate(List<T> items, Pageable pageable) {
        int start = (int) pageable.getOffset();

        if (start >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        }

        int end = Math.min(start + pageable.getPageSize(), items.size());

        return new PageImpl<>(
                items.subList(start, end),
                pageable,
                items.size()
        );
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < 8; i++) {
            Cell cell = row.getCell(i);

            if (cell != null && !cell.toString().trim().isBlank()) {
                return false;
            }
        }

        return true;
    }

    private void activateProduct(Product product) {
        product.setStatus(true);
        product.setDeleted(false);
        product.setDeletedAt(null);
    }

    private void applyProductInfo(Product product, ProductCreationRequest request, Category category) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setBrandName(request.getBrandName());
        product.setExpiryDate(request.getExpiryDate());
        product.setCategory(category);
    }

    private String generateProductCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}
