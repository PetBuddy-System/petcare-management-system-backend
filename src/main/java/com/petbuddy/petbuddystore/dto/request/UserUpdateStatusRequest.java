package com.petbuddy.petbuddystore.dto.request;

import com.petbuddy.petbuddystore.common.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateStatusRequest {
    UserStatus status;
}
