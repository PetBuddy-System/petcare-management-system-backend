package com.petbuddy.petbuddystore.dto.response;

import com.petbuddy.petbuddystore.common.enums.PetStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PetResponse {
    String petId;
    String userId;
    String petName;
    String species;
    String breed;
    String gender;
    Integer age;
    Double weight;
    String color;
    String healthNote;
    String behaviorNote;
    String avatarUrl;
    PetStatus petStatus;
    LocalDateTime createdAt;

}
