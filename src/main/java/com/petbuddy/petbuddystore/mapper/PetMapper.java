package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.request.PetCreationRequest;
import com.petbuddy.petbuddystore.dto.request.PetUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.PetResponse;
import com.petbuddy.petbuddystore.model.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PetMapper {
    Pet toPet(PetCreationRequest request);

    @Mapping(source = "user.userId", target = "userId")
    PetResponse toPetResponse(Pet pet);

    void updatePet(PetUpdateRequest request, @MappingTarget Pet pet);
}
