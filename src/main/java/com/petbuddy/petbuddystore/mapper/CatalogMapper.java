package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.request.CatalogCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CatalogUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CatalogResponse;
import com.petbuddy.petbuddystore.model.Catalog;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CatalogMapper {
    Catalog toCatalog(CatalogCreationRequest request);

    CatalogResponse toCatalogResponse(Catalog catalog);

    void updateCatalog(CatalogUpdateRequest updateRequest, @MappingTarget Catalog catalog);

}
