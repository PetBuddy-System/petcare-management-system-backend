package com.petbuddy.petbuddystore.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PetUpdateRequest {
    String petName;
    String species;
    String breed;
    String gender;
    Integer age;
    Double weight;
    String color;
    String healthNote;
    String behaviorNote;
}
