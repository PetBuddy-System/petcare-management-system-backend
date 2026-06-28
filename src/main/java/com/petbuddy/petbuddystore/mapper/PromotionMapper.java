package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.request.PromotionRequest;
import com.petbuddy.petbuddystore.dto.response.PromotionResponse;
import com.petbuddy.petbuddystore.dto.response.PromotionDetailResponse;
import com.petbuddy.petbuddystore.model.Promotion;
import com.petbuddy.petbuddystore.model.PromotionDetail;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PromotionMapper {

    @Mapping(target = "promotionDetails", ignore = true)
    Promotion toPromotion(PromotionRequest request);

    PromotionResponse toPromotionResponse(Promotion promotion);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productCode", source = "product.productCode")
    @Mapping(target = "price", source = "product.price")
    @Mapping(target = "discountedPrice", ignore = true)
    PromotionDetailResponse toPromotionDetailResponse(PromotionDetail detail);

    @AfterMapping
    default void calculateDiscountedPrice(PromotionDetail detail, @MappingTarget PromotionDetailResponse response) {
        if (detail.getProduct() != null && detail.getProduct().getPrice() != null && detail.getDiscountValue() != null && detail.getDiscountType() != null) {
            java.math.BigDecimal originalPrice = detail.getProduct().getPrice();
            java.math.BigDecimal discountValue = detail.getDiscountValue();
            java.math.BigDecimal discounted;
            
            if (detail.getDiscountType() == com.petbuddy.petbuddystore.common.enums.DiscountType.PERCENTAGE) {
                java.math.BigDecimal multiplier = java.math.BigDecimal.ONE.subtract(discountValue.divide(java.math.BigDecimal.valueOf(100)));
                discounted = originalPrice.multiply(multiplier);
            } else {
                discounted = originalPrice.subtract(discountValue);
            }
            if (discounted.compareTo(java.math.BigDecimal.ZERO) < 0) {
                discounted = java.math.BigDecimal.ZERO;
            }
            response.setDiscountedPrice(discounted);
        }
    }

    @Mapping(target = "promotionDetails", ignore = true)
    void updatePromotionFromRequest(PromotionRequest request, @MappingTarget Promotion promotion);
}
