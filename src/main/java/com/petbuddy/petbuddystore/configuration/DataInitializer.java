package com.petbuddy.petbuddystore.configuration;

import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        initCategories();
    }

    private void initCategories() {
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