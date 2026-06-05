package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.CageCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CageUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CageResponse;

import java.util.List;

public interface CageService {
    CageResponse createCage(CageCreationRequest request);
    List<CageResponse> getAllCages();
    CageResponse getCageById(Long cageId);
    CageResponse updateCage(CageUpdateRequest request, Long cageId);
}
