package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.ProductStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.ProductBatchCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductBatchUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductBatchResponse;
import com.petbuddy.petbuddystore.dto.response.ProductImportResponse;
import com.petbuddy.petbuddystore.mapper.ProductBatchMapper;
import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.model.ProductBatch;
import com.petbuddy.petbuddystore.repository.ProductBatchRepository;
import com.petbuddy.petbuddystore.service.CategoryService;
import com.petbuddy.petbuddystore.service.FileService;
import com.petbuddy.petbuddystore.service.ProductBatchService;
import com.petbuddy.petbuddystore.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductBatchServiceImpl implements ProductBatchService {

    static final int MAX_BATCH_CREATE_LIMIT = 10;
    static final int IMAGE_COL_START = 9;  // columns 9-12 → Image1-Image4
    static final int IMAGE_COL_END = 12;

    ProductBatchRepository productBatchRepository;
    ProductService productService;
    ProductBatchMapper productBatchMapper;
    CategoryService categoryService;
    FileService fileService;

    @Override
    @Transactional
    public List<ProductBatchResponse> createBatches(UUID productId, List<ProductBatchCreationRequest> requests) {
        validateBatchCreateRequests(requests);
        Product product = productService.getActiveProductEntityById(productId);
        long sequence = product.getLastBatchSequence();
        List<ProductBatch> batches = new ArrayList<>();
        for (ProductBatchCreationRequest request : requests) {
            sequence++;
            ProductBatch batch = productBatchMapper.toProductBatch(request);
            batch.setProduct(product);
            batch.setBatchCode(generateBatchCode(product, sequence));
            batches.add(batch);
        }
        product.setLastBatchSequence(sequence);
        return productBatchRepository.saveAllAndFlush(batches)
                .stream()
                .map(this::buildBatchResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductBatchResponse> getBatchesByProduct(UUID productId, String keyword, ProductStatus status, String sortBy, Pageable pageable) {
        Product product = productService.getProductEntityById(productId);
        Pageable sortedPageable = buildPageable(pageable, sortBy);
        Specification<ProductBatch> spec = buildBatchSpec(product.getProductId(), keyword, status);
        return productBatchRepository.findAll(spec, sortedPageable).map(this::buildBatchResponse);
    }

    @Override
    @Transactional
    public ProductBatchResponse updateBatch(UUID batchId, ProductBatchUpdateRequest request) {
        ProductBatch batch = getBatchEntityById(batchId);
        if (request.getStockQuantity() != null) {
            if (request.getStockQuantity() < 0) {throw new AppException(ErrorCode.PRODUCT_STOCK_INVALID);}
            batch.setStockQuantity(request.getStockQuantity());
        }
        if (request.getExpiryDate() != null) {batch.setExpiryDate(request.getExpiryDate());}
        if (request.getStatus() != null) {updateStatus(batch, request.getStatus());}
        return buildBatchResponse(productBatchRepository.save(batch));
    }

    @Override
    @Transactional
    public void deleteDeletedBatchesOlderThan90Days() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        List<ProductBatch> oldDeletedBatches = productBatchRepository.findByStatusAndDeletedAtBefore(ProductStatus.DELETED, threshold);
        productBatchRepository.deleteAll(oldDeletedBatches);
    }

    private void validateBatchCreateRequests(List<ProductBatchCreationRequest> requests) {
        if (requests == null || requests.isEmpty()) {throw new AppException(ErrorCode.BATCH_REQUIRED);}
        if (requests.size() > MAX_BATCH_CREATE_LIMIT) {throw new AppException(ErrorCode.BATCH_LIMIT_EXCEEDED);}
    }

    private void updateStatus(ProductBatch batch, ProductStatus status) {
        batch.setStatus(status);
        if (status == ProductStatus.DELETED) {batch.setDeletedAt(LocalDateTime.now());
        } else {batch.setDeletedAt(null);}
    }

    private String generateBatchCode(Product product, long sequence) {
        long letterPart = (sequence - 1) / 999;
        long numberPart = (sequence - 1) % 999 + 1;
        char c1 = (char) ('A' + (letterPart / 26) % 26);
        char c2 = (char) ('A' + letterPart % 26);
        return product.getProductCode() + "-" + c1 + c2 + String.format("%03d", numberPart);
    }

    private ProductBatch getBatchEntityById(UUID batchId) {
        return productBatchRepository.findById(batchId).orElseThrow(() -> new AppException(ErrorCode.BATCH_NOT_FOUND));
    }

    private Pageable buildPageable(Pageable pageable, String sortBy) {
        Sort sort = switch (sortBy == null ? "date_desc" : sortBy) {
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "stock_asc" -> Sort.by(Sort.Direction.ASC, "stockQuantity");
            case "stock_desc" -> Sort.by(Sort.Direction.DESC, "stockQuantity");
            case "expiry_asc" -> Sort.by(Sort.Direction.ASC, "expiryDate");
            case "expiry_desc" -> Sort.by(Sort.Direction.DESC, "expiryDate");
            default -> throw new AppException(ErrorCode.INVALID_SORT_OPTION);
        };
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private Specification<ProductBatch> buildBatchSpec(UUID productId, String keyword, ProductStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("product").get("productId"), productId));
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("batchCode")), "%" + keyword.trim().toLowerCase() + "%"));
            }
            if (status != null) {predicates.add(cb.equal(root.get("status"), status));} else {
                predicates.add(cb.notEqual(root.get("status"), ProductStatus.DELETED));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ProductBatchResponse buildBatchResponse(ProductBatch batch) {
        ProductBatchResponse response = productBatchMapper.toProductBatchResponse(batch);
        if (batch.getProduct() != null) {
            response.setProductId(batch.getProduct().getProductId());
            response.setProductCode(batch.getProduct().getProductCode());
            response.setProductName(batch.getProduct().getName());
        }
        return response;
    }

    @Override
    @Transactional
    public ProductImportResponse importProductsAndBatches(MultipartFile file, boolean confirm) {
        validateExcelFile(file);
        log.info("Import file size: {}", file.getSize());
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<PendingImportRow> validRows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet.getRow(0), formatter);

            Map<String, EmbeddedImage> inCellImageMap = (sheet instanceof XSSFSheet)
                    ? extractInCellImages(file) : Collections.emptyMap();

            // Build image map: "rowIndex_colIndex" -> EmbeddedImage (only available for XSSF/xlsx)
            Map<String, EmbeddedImage> imageMap = (sheet instanceof XSSFSheet xssfSheet)
                    ? buildImageMap(xssfSheet, inCellImageMap) : Collections.emptyMap();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmpty(row, formatter)) {continue;}
                int rowNumber = i + 1;
                String name = cell(row, 0, formatter);
                String description = cell(row, 1, formatter);
                BigDecimal price = parseDecimal(cell(row, 2, formatter));
                String brandName = cell(row, 3, formatter);
                String categoryName = cell(row, 4, formatter);
                BigDecimal stockDecimal = parseDecimal(cell(row, 5, formatter));
                Integer stockQuantity = stockDecimal == null ? null : stockDecimal.intValue();
                LocalDate expiryDate = parseDate(row.getCell(6), cell(row, 6, formatter));
                String ingredients = cell(row, 7, formatter);
                String usageInstructions = cell(row, 8, formatter);

                // Collect up to 4 embedded images from cols 9-12 (optional)
                List<EmbeddedImage> rowImages = new ArrayList<>();
                for (int col = IMAGE_COL_START; col <= IMAGE_COL_END; col++) {
                    EmbeddedImage img = imageMap.get(i + "_" + col);
                    if (img != null) {rowImages.add(img);}
                }

                if (name == null || name.isBlank()) {errors.add("Row " + rowNumber + ": Product name is required.");continue;}
                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {errors.add("Row " + rowNumber + ": Product price is invalid.");continue;}
                if (categoryName == null || categoryName.isBlank()) {errors.add("Row " + rowNumber + ": Category name is required.");continue;}
                if (stockQuantity == null || stockQuantity < 0) {errors.add("Row " + rowNumber + ": Stock quantity is invalid.");continue;}
                if (expiryDate != null && !expiryDate.isAfter(LocalDate.now())) {errors.add("Row " + rowNumber + ": Expiry date is invalid.");continue;}

                Category category;
                try {category = categoryService.getActiveCategoryEntityByName(categoryName);
                } catch (AppException e) {
                    errors.add("Row " + rowNumber + ": Category '" + categoryName + "' does not exist or is inactive.");continue;
                }

                Product product = productService.getProductEntityByName(name);

                if (product != null) {
                    if (product.getStatus() == ProductStatus.DELETED) {
                        errors.add("Row " + rowNumber + ": Product '" + name + "' is deleted.");continue;
                    }

                    if (hasDifferentProductInfo(product, description, price, brandName, category, ingredients, usageInstructions)) {
                        warnings.add("Row " + rowNumber + ": Product '" + name + "' already exists. Old product information will be kept, only new batch will be created.");
                    }
                }
                validRows.add(new PendingImportRow(rowNumber, name, description, price, brandName, category, stockQuantity, expiryDate, ingredients, usageInstructions, rowImages));
            }

            if (!errors.isEmpty() || !confirm) {
                return ProductImportResponse.builder().canImport(errors.isEmpty()).createdProducts(0).createdBatches(0).warnings(warnings).errors(errors).build();
            }
            int createdProducts = 0;
            int createdBatches = 0;
            Map<UUID, Long> batchCounter = new HashMap<>();
            for (PendingImportRow row : validRows) {
                Product product = productService.getProductEntityByName(row.name());
                if (product == null) {
                    // Upload embedded images only for new products
                    List<String> imageUrls = uploadEmbeddedImages(row.images(), row.name());
                    product = productService.createProductFromImport(row.name(), row.description(), row.price(), row.brandName(), row.category(), row.ingredients(), row.usageInstructions(), imageUrls);
                    createdProducts++;
                }
                Product finalProduct = product;
                long nextNumber = batchCounter.computeIfAbsent(
                        product.getProductId(),
                        productId -> finalProduct.getLastBatchSequence() + 1
                );
                ProductBatch batch = ProductBatch.builder().batchCode(generateBatchCode(product, nextNumber)).product(product).stockQuantity(row.stockQuantity()).expiryDate(row.expiryDate()).status(ProductStatus.ACTIVE).build();
                productBatchRepository.save(batch);
                productService.updateLastBatchSequence(product, nextNumber);
                batchCounter.put(product.getProductId(), nextNumber + 1);
                createdBatches++;
            }
            return ProductImportResponse.builder()
                    .canImport(true)
                    .createdProducts(createdProducts)
                    .createdBatches(createdBatches)
                    .warnings(warnings)
                    .errors(errors).build();

        } catch (AppException e) {throw e;
        } catch (Exception e) {log.error("Import products and batches failed", e);
            throw new AppException(ErrorCode.IMPORT_FAILED);
        }
    }

    private Map<String, EmbeddedImage> buildImageMap(XSSFSheet sheet, Map<String, EmbeddedImage> inCellImageMap) {
        Map<String, EmbeddedImage> map = new HashMap<>();

        // 1. Process in-cell images from formulas or text values
        DataFormatter formatter = new DataFormatter();
        for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            for (int col = IMAGE_COL_START; col <= IMAGE_COL_END; col++) {
                Cell cell = row.getCell(col);
                if (cell == null) continue;

                String cellText = null;
                if (cell.getCellType() == CellType.FORMULA) {
                    try {
                        cellText = cell.getCellFormula();
                        log.info("Cell formula at row={}, col={} is: {}", r + 1, col, cellText);
                    } catch (Exception ignored) {}
                }

                if (cellText == null || cellText.isEmpty()) {
                    try {
                        cellText = formatter.formatCellValue(cell);
                    } catch (Exception ignored) {}
                }

                if (cellText != null && !cellText.isEmpty()) {
                    String imageId = extractImageId(cellText);
                    if (imageId != null) {
                        EmbeddedImage img = inCellImageMap.get(imageId);
                        if (img != null) {
                            map.put(r + "_" + col, img);
                            log.info("Mapped in-cell image with ID {} to cell {}_{}", imageId, r, col);
                        } else {
                            log.warn("In-cell image with ID {} found in cell but not present in ZIP archive", imageId);
                        }
                    }
                }
            }
        }

        // 2. Process floating images (drawings)
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing != null) {
            List<XSSFPicture> pictures = new ArrayList<>();
            for (XSSFShape shape : drawing.getShapes()) {
                extractPictures(shape, pictures);
            }
            for (XSSFPicture picture : pictures) {
                XSSFAnchor anchor = picture.getAnchor();
                if (!(anchor instanceof XSSFClientAnchor clientAnchor)) {continue;}
                int row1 = clientAnchor.getRow1();
                int row2 = clientAnchor.getRow2();
                int col1 = clientAnchor.getCol1();
                int col2 = clientAnchor.getCol2();

                int row = (row2 >= row1) ? (row1 + row2) / 2 : row1;
                int col = (col2 >= col1) ? (col1 + col2) / 2 : col1;

                if (col >= IMAGE_COL_START && col <= IMAGE_COL_END) {
                    XSSFPictureData pd = picture.getPictureData();
                    if (pd != null) {
                        map.putIfAbsent(row + "_" + col, new EmbeddedImage(pd.getData(), pd.getMimeType()));
                        log.info("Mapped floating image to cell {}_{}", row, col);
                    }
                }
            }
        }

        return map;
    }

    private void extractPictures(XSSFShape shape, List<XSSFPicture> pictures) {
        if (shape instanceof XSSFPicture picture) {
            pictures.add(picture);
        } else if (shape instanceof XSSFShapeGroup group) {
            for (XSSFShape child : group) {
                extractPictures(child, pictures);
            }
        }
    }

    private String extractImageId(String cellText) {
        if (cellText == null || !cellText.contains("DISPIMG")) {
            return null;
        }
        int startIndex = cellText.indexOf("\"ID_");
        if (startIndex == -1) {
            startIndex = cellText.indexOf("'ID_");
        }
        if (startIndex != -1) {
            int endIndex = cellText.indexOf("\"", startIndex + 1);
            if (endIndex == -1) {
                endIndex = cellText.indexOf("'", startIndex + 1);
            }
            if (endIndex != -1) {
                return cellText.substring(startIndex + 1, endIndex);
            }
        }

        int idIdx = cellText.indexOf("ID_");
        if (idIdx != -1) {
            int endIdx = idIdx;
            while (endIdx < cellText.length() &&
                   (Character.isLetterOrDigit(cellText.charAt(endIdx)) || cellText.charAt(endIdx) == '_')) {
                endIdx++;
            }
            return cellText.substring(idIdx, endIdx);
        }
        return null;
    }

    private Map<String, EmbeddedImage> extractInCellImages(MultipartFile file) {
        Map<String, EmbeddedImage> imageMap = new HashMap<>();
        try {
            Map<String, byte[]> zipContent = new HashMap<>();
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;
                    zipContent.put(entry.getName(), zis.readAllBytes());
                }
            }

            byte[] cellimagesXml = zipContent.get("xl/cellimages.xml");
            byte[] cellimagesRels = zipContent.get("xl/_rels/cellimages.xml.rels");

            if (cellimagesXml == null || cellimagesRels == null) {
                return imageMap;
            }

            Map<String, String> rels = new HashMap<>();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document relsDoc = dBuilder.parse(new ByteArrayInputStream(cellimagesRels));
            NodeList relationshipList = relsDoc.getElementsByTagNameNS("*", "Relationship");
            for (int i = 0; i < relationshipList.getLength(); i++) {
                Element rel = (Element) relationshipList.item(i);
                String rId = rel.getAttribute("Id");
                String target = rel.getAttribute("Target");
                if (rId != null && !rId.isEmpty() && target != null && !target.isEmpty()) {
                    String cleanPath = target;
                    if (cleanPath.startsWith("../")) {
                        cleanPath = cleanPath.substring(3);
                    }
                    if (!cleanPath.startsWith("xl/")) {
                        cleanPath = "xl/" + cleanPath;
                    }
                    rels.put(rId, cleanPath);
                }
            }

            Document imgDoc = dBuilder.parse(new ByteArrayInputStream(cellimagesXml));
            NodeList cellImageList = imgDoc.getElementsByTagNameNS("*", "cellImage");
            for (int i = 0; i < cellImageList.getLength(); i++) {
                Element cellImage = (Element) cellImageList.item(i);

                NodeList cNvPrList = cellImage.getElementsByTagNameNS("*", "cNvPr");
                if (cNvPrList.getLength() == 0) continue;
                Element cNvPr = (Element) cNvPrList.item(0);
                String imageId = cNvPr.getAttribute("name");

                NodeList blipList = cellImage.getElementsByTagNameNS("*", "blip");
                if (blipList.getLength() == 0) continue;
                Element blip = (Element) blipList.item(0);

                String rId = blip.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed");
                if (rId == null || rId.isEmpty()) {
                    rId = blip.getAttribute("r:embed");
                }
                if (rId == null || rId.isEmpty()) {
                    rId = blip.getAttribute("embed");
                }

                if (imageId != null && !imageId.isEmpty() && rId != null && !rId.isEmpty()) {
                    String targetPath = rels.get(rId);
                    if (targetPath != null) {
                        byte[] imgData = zipContent.get(targetPath);
                        if (imgData != null && imgData.length > 0) {
                            String mimeType = getMimeTypeFromPath(targetPath);
                            imageMap.put(imageId, new EmbeddedImage(imgData, mimeType));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse in-cell images from xlsx: {}", e.getMessage(), e);
        }
        return imageMap;
    }

    private String getMimeTypeFromPath(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        return "image/jpeg";
    }

    private List<String> uploadEmbeddedImages(List<EmbeddedImage> images, String productName) {
        if (images == null || images.isEmpty()) {return Collections.emptyList();}
        List<String> urls = new ArrayList<>();
        for (EmbeddedImage img : images) {
            try {
                urls.add(fileService.uploadProductImageFromBytes(img.data(), img.mimeType()));
            } catch (Exception e) {
                log.warn("Skipped image upload for product '{}': {}", productName, e.getMessage());
            }
        }
        return urls;
    }

    private String cell(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        if (cell == null || cell.getCellType() == CellType.BLANK) {return null;}
        String value = formatter.formatCellValue(cell).trim();
        return value.isBlank() ? null : value;
    }

    private BigDecimal parseDecimal(String value) {
        try {if (value == null) {return null;}
            String normalizedValue = value.replace(",", "").replace(" ", "").trim();
            return new BigDecimal(normalizedValue);
        } catch (Exception e) {return null;}
    }

    private LocalDate parseDate(Cell cell, String value) {
        try {
            if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            if (value == null) {return null;}
            List<DateTimeFormatter> formats = List.of(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );
            for (DateTimeFormatter formatter : formats) {
                try {return LocalDate.parse(value, formatter);
                } catch (Exception ignored) {
                }
            }
            return null;
        } catch (Exception e) {return null;}
    }

    private boolean isEmpty(Row row, DataFormatter formatter) {
        for (int i = 0; i <= 8; i++) {
            if (cell(row, i, formatter) != null) {return false;}
        }
        return true;
    }

    private void validateHeader(Row header, DataFormatter formatter) {
        if (header == null) {throw new AppException(ErrorCode.INVALID_EXCEL_TEMPLATE);}
        String[] expectedHeaders = {"Name", "Description", "Price", "BrandName", "CategoryName", "StockQuantity", "ExpiryDate", "Ingredients", "UsageInstructions", "Image1", "Image2", "Image3", "Image4"};
        for (int i = 0; i < expectedHeaders.length; i++) {
            String actual = cell(header, i, formatter);
            if (actual == null || !expectedHeaders[i].equalsIgnoreCase(actual)) {
                throw new AppException(ErrorCode.INVALID_EXCEL_TEMPLATE);
            }
        }
    }

    private boolean hasDifferentProductInfo(Product product, String description, BigDecimal price, String brandName, Category category, String ingredients, String usageInstructions) {
        return !Objects.equals(product.getDescription(), description)
                || product.getPrice().compareTo(price) != 0
                || !Objects.equals(product.getBrandName(), brandName)
                || !Objects.equals(product.getCategory().getCategoryId(), category.getCategoryId())
                || !Objects.equals(product.getIngredients(), ingredients)
                || !Objects.equals(product.getUsageInstructions(), usageInstructions);
    }

    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {throw new AppException(ErrorCode.FILE_REQUIRED);}
        if (file.getSize() > 10 * 1024 * 1024) {throw new AppException(ErrorCode.FILE_TOO_LARGE);}
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {throw new AppException(ErrorCode.INVALID_FILE_TYPE);}
    }

    private record EmbeddedImage(byte[] data, String mimeType) {}

    private record PendingImportRow(int rowNumber, String name, String description, BigDecimal price, String brandName, Category category, Integer stockQuantity, LocalDate expiryDate, String ingredients, String usageInstructions, List<EmbeddedImage> images) {}
}