package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.common.enums.PromotionStatus;
import com.petbuddy.petbuddystore.model.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, UUID> {

    List<PromotionDetail> findByProduct_ProductIdInAndPromotion_Status(
            List<UUID> productIds,
            PromotionStatus status
    );
}
