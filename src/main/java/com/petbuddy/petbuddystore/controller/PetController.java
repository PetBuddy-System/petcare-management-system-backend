package com.petbuddy.petbuddystore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.PetCreationRequest;
import com.petbuddy.petbuddystore.dto.request.PetUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.PetResponse;
import com.petbuddy.petbuddystore.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Pet API", description = "Quản lí hồ sơ thú cưng")
public class PetController {
    PetService petService;
    ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Tạo mới hồ sơ Pet")
    public ResponseEntity<ApiResponse<PetResponse>> createPet(@RequestPart("data") String requestJson,
                                                               @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        PetCreationRequest request = objectMapper.readValue(requestJson, PetCreationRequest.class);
        PetResponse response = petService.createPet(request, image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pet created successfully", response));
    }

    @GetMapping("/{petId}")
    @Operation(description = "Tìm pet theo id")
    public ResponseEntity<ApiResponse<PetResponse>> getPetById(@PathVariable String petId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(petService.getPetById(petId)));
    }

    @PutMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetResponse>> updatePet(
            @PathVariable String petId,
            @RequestPart("data") String requestJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        PetUpdateRequest request = objectMapper.readValue(requestJson, PetUpdateRequest.class);
        PetResponse response = petService.updatePet(petId, request, image);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Pet updated successfully", response));
    }
}
