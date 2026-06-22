package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.CageStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.CageCreationRequest;
import com.petbuddy.petbuddystore.dto.request.CageUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.CageResponse;
import com.petbuddy.petbuddystore.mapper.CageMapper;
import com.petbuddy.petbuddystore.model.Cage;
import com.petbuddy.petbuddystore.repository.CageRepository;
import com.petbuddy.petbuddystore.service.CageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CageServiceImpl implements CageService {
    CageRepository cageRepository;
    CageMapper cageMapper;

    @Override
    public CageResponse createCage(CageCreationRequest request) {
        if (cageRepository.existsByCageCode(request.getCageCode())) {
            throw new AppException(ErrorCode.CAGE_EXISTED);
        }

        Cage cage = cageMapper.toCage(request);
        cage.setCageStatus(CageStatus.AVAILABLE);
        return cageMapper.toCageResponse(cageRepository.save(cage));
    }

    @Override
    public List<CageResponse> getAllCages() {
        return cageRepository.findAll()
                .stream()
                .map(cageMapper::toCageResponse).toList();
    }

    @Override
    public CageResponse getCageById(Long cageId) {
        Cage cage = cageRepository.findById(cageId)
                .orElseThrow(() -> new AppException(ErrorCode.CAGE_NOT_EXISTED));
        return cageMapper.toCageResponse(cage);
    }

    @Override
    public CageResponse updateCage(CageUpdateRequest request, Long cageId) {
        Cage cage = cageRepository.findById(cageId)
                .orElseThrow(() -> new AppException(ErrorCode.CAGE_NOT_EXISTED));
        cageMapper.updateCage(cage, request);
        return cageMapper.toCageResponse(cage);
    }
}
