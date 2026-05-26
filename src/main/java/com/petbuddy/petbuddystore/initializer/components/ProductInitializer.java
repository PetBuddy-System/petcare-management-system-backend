package com.petbuddy.petbuddystore.initializer.components;

import com.petbuddy.petbuddystore.model.Category;
import com.petbuddy.petbuddystore.model.Product;
import com.petbuddy.petbuddystore.repository.CategoryRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductInitializer {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public void init() {

        if (productRepository.count() > 0) {
            return;
        }

        Category dogFood = categoryRepository.findByNameIgnoreCase("Dog Food")
                .orElseThrow(() -> new RuntimeException("Dog Food category not found"));

        Category catFood = categoryRepository.findByNameIgnoreCase("Cat Food")
                .orElseThrow(() -> new RuntimeException("Cat Food category not found"));

        Category accessories = categoryRepository.findByNameIgnoreCase("Pet Accessories")
                .orElseThrow(() -> new RuntimeException("Pet Accessories category not found"));

        Category supplement = categoryRepository.findByNameIgnoreCase("Pet Supplement")
                .orElseThrow(() -> new RuntimeException("Pet Supplement category not found"));

        Category toys = categoryRepository.findByNameIgnoreCase("Pet Toys")
                .orElseThrow(() -> new RuntimeException("Pet Toys category not found"));

        Category hygiene = categoryRepository.findByNameIgnoreCase("Pet Hygiene")
                .orElseThrow(() -> new RuntimeException("Pet Hygiene category not found"));

        List<Product> products = List.of(

                Product.builder()
                        .name("Royal Canin Mini Adult")
                        .description("Premium dry food for adult small breed dogs")
                        .price(BigDecimal.valueOf(450000))
                        .stockQuantity(50)
                        .imageUrl("https://example.com/dog-food-1.jpg")
                        .brandName("Royal Canin")
                        .category(dogFood)
                        .build(),

                Product.builder()
                        .name("Pedigree Puppy Chicken")
                        .description("Nutritious puppy food with chicken flavor")
                        .price(BigDecimal.valueOf(320000))
                        .stockQuantity(40)
                        .imageUrl("https://example.com/dog-food-2.jpg")
                        .brandName("Pedigree")
                        .category(dogFood)
                        .build(),

                Product.builder()
                        .name("Whiskas Ocean Fish")
                        .description("Cat food with ocean fish flavor")
                        .price(BigDecimal.valueOf(210000))
                        .stockQuantity(35)
                        .imageUrl("https://example.com/cat-food-1.jpg")
                        .brandName("Whiskas")
                        .category(catFood)
                        .build(),

                Product.builder()
                        .name("Me-O Persian Cat")
                        .description("Special nutrition for Persian cats")
                        .price(BigDecimal.valueOf(280000))
                        .stockQuantity(25)
                        .imageUrl("https://example.com/cat-food-2.jpg")
                        .brandName("Me-O")
                        .category(catFood)
                        .build(),

                Product.builder()
                        .name("Adjustable Pet Collar")
                        .description("Comfortable adjustable collar for pets")
                        .price(BigDecimal.valueOf(120000))
                        .stockQuantity(100)
                        .imageUrl("https://example.com/accessory-1.jpg")
                        .brandName("PetBuddy")
                        .category(accessories)
                        .build(),

                Product.builder()
                        .name("Pet Carrier Backpack")
                        .description("Portable backpack for carrying pets safely")
                        .price(BigDecimal.valueOf(650000))
                        .stockQuantity(15)
                        .imageUrl("https://example.com/accessory-2.jpg")
                        .brandName("PetTravel")
                        .category(accessories)
                        .build(),

                Product.builder()
                        .name("Vitamin Gel for Dogs")
                        .description("Daily vitamin supplement for healthy dogs")
                        .price(BigDecimal.valueOf(180000))
                        .stockQuantity(45)
                        .imageUrl("https://example.com/supplement-1.jpg")
                        .brandName("BioPet")
                        .category(supplement)
                        .build(),

                Product.builder()
                        .name("Omega 3 for Cats")
                        .description("Omega 3 supplement for skin and fur health")
                        .price(BigDecimal.valueOf(220000))
                        .stockQuantity(30)
                        .imageUrl("https://example.com/supplement-2.jpg")
                        .brandName("HealthyPet")
                        .category(supplement)
                        .build(),

                Product.builder()
                        .name("Rubber Chew Bone")
                        .description("Durable rubber chew toy for dogs")
                        .price(BigDecimal.valueOf(90000))
                        .stockQuantity(80)
                        .imageUrl("https://example.com/toy-1.jpg")
                        .brandName("PetFun")
                        .category(toys)
                        .build(),

                Product.builder()
                        .name("Interactive Cat Ball")
                        .description("Interactive toy ball for active cats")
                        .price(BigDecimal.valueOf(110000))
                        .stockQuantity(60)
                        .imageUrl("https://example.com/toy-2.jpg")
                        .brandName("CatPlay")
                        .category(toys)
                        .build(),

                Product.builder()
                        .name("Pet Shampoo Aloe Vera")
                        .description("Gentle shampoo for pet hygiene")
                        .price(BigDecimal.valueOf(150000))
                        .stockQuantity(70)
                        .imageUrl("https://example.com/hygiene-1.jpg")
                        .brandName("PetCare")
                        .category(hygiene)
                        .build(),

                Product.builder()
                        .name("Pet Ear Cleaning Solution")
                        .description("Cleaning solution for pet ears")
                        .price(BigDecimal.valueOf(130000))
                        .stockQuantity(40)
                        .imageUrl("https://example.com/hygiene-2.jpg")
                        .brandName("VetClean")
                        .category(hygiene)
                        .build()
        );

        productRepository.saveAll(products);
    }
}