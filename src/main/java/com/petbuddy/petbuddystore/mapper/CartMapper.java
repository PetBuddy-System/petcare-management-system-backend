package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.response.CartItemResponse;
import com.petbuddy.petbuddystore.dto.response.CartResponse;
import com.petbuddy.petbuddystore.session.CartItemSession;
import com.petbuddy.petbuddystore.session.CartSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartResponse toCartResponse(CartSession cartSession);

    CartItemResponse toCartItemResponse(CartItemSession item);
}
