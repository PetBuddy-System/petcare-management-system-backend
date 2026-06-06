package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.request.ProductCreationRequest;
import com.petbuddy.petbuddystore.dto.request.ProductUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.ProductResponse;
import com.petbuddy.petbuddystore.model.Product;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface ProductMapper {

    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "productId", ignore = true)
    Product toProduct(ProductCreationRequest request);

    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toProductResponse(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateProduct(
            @MappingTarget Product product,
            ProductUpdateRequest request
    );
}