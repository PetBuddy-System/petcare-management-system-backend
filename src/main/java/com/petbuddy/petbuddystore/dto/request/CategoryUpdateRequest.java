package com.petbuddy.petbuddystore.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {

    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    String name;

    String description;

    Boolean status;
}