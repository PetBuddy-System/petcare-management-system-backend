package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.PetStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.PetCreationRequest;
import com.petbuddy.petbuddystore.dto.request.PetUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.PetResponse;
import com.petbuddy.petbuddystore.mapper.PetMapper;
import com.petbuddy.petbuddystore.model.Pet;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.repository.PetRepository;
import com.petbuddy.petbuddystore.service.FileService;
import com.petbuddy.petbuddystore.service.PetService;
import com.petbuddy.petbuddystore.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PetServiceImpl implements PetService {
    PetRepository petRepository;
    PetMapper petMapper;
    FileService fileService;
    UserService userService;

    @Override
    public PetResponse createPet(PetCreationRequest request, MultipartFile image) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserEntityById(userId);

        Pet pet = petMapper.toPet(request);
        pet.setUser(user);
        pet.setPetStatus(PetStatus.ACTIVE);

        if (!image.isEmpty()){
            String avatarUrl = fileService.uploadPetImage(image);
            pet.setAvatarUrl(avatarUrl);
        }
        return petMapper.toPetResponse(petRepository.save(pet));
    }

    @Override
    public PetResponse getPetById(String petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new AppException(ErrorCode.PET_NOT_EXISTED));
        return petMapper.toPetResponse(pet);
    }

    @Override
    public PetResponse updatePet(String petId, PetUpdateRequest request, MultipartFile image) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new AppException(ErrorCode.PET_NOT_EXISTED));
        petMapper.updatePet(request, pet);

        if (!image.isEmpty()){
            String avatarUrl = fileService.uploadPetImage(image);
            pet.setAvatarUrl(avatarUrl);
        }
        return petMapper.toPetResponse(petRepository.save(pet));
    }
}
