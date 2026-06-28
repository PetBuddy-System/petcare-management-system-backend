package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.common.enums.PromotionStatus;
import com.petbuddy.petbuddystore.dto.request.PromotionRequest;
import com.petbuddy.petbuddystore.dto.response.PromotionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest request);

    Page<PromotionResponse> getPromotions(String keyword, PromotionStatus status, Pageable pageable, String sortBy);

    PromotionResponse getPromotionById(UUID id);

    PromotionResponse updatePromotion(UUID id, PromotionRequest request);
}
