package com.petbuddy.petbuddystore.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffOrderResponse {
    Long orderId;
    String orderCode;
    String customerName;
    String customerPhone;
    String address;
    String status;
    BigDecimal totalAmount;
    LocalDateTime createAt;

}
