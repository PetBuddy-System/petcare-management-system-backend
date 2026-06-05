package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.response.CartItemResponse;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.session.CartItemSession;
import com.petbuddy.petbuddystore.session.CartSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartResponse toCartResponse(CartSession cartSession);
    @Mapping(
            target = "subtotal",
            expression =
                    "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))"
    )
    CartItemResponse toCartItemResponse(
            CartItemSession item
    );
}
