package com.petbuddy.petbuddystore.initializer.components;

import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;

    public void init() {

        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> categories = List.of(

                Category.builder()
                        .name("Dog Food")
                        .description("Food for dogs")
                        .build(),

                Category.builder()
                        .name("Cat Food")
                        .description("Food for cats")
                        .build(),

                Category.builder()
                        .name("Pet Accessories")
                        .description("Accessories for pets")
                        .build(),

                Category.builder()
                        .name("Pet Supplement")
                        .description("Functional food and supplements for pets, not medicine")
                        .build(),

                Category.builder()
                        .name("Pet Toys")
                        .description("Toys and entertainment products for pets")
                        .build(),

                Category.builder()
                        .name("Pet Hygiene")
                        .description("Hygiene and grooming products for pets")
                        .build()
        );

        categoryRepository.saveAll(categories);
    }
}