package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.PetCreationRequest;
import com.petbuddy.petbuddystore.dto.request.PetUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.PetResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PetService {
    PetResponse createPet(PetCreationRequest request, MultipartFile image);
    PetResponse getPetById(String petId);
    PetResponse updatePet(String petId, PetUpdateRequest request, MultipartFile image);
}
