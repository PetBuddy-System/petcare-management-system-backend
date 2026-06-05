package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.request.CageCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CageUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CageResponse;
import com.petbuddy.petbuddystore.model.Cage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CageMapper {
    Cage toCage(CageCreationRequest request);
    CageResponse toCageResponse(Cage cage);

    void updateCage(@MappingTarget Cage cage, CageUpdateRequest request);
}
