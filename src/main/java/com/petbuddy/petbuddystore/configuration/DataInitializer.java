package com.petbuddy.petbuddystore.configuration;

import com.petbuddy.petbuddystore.repository.CategoryRepository;
import com.petbuddy.petbuddystore.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;

public class DataInitializer implements CommandLineRunner {
    CategoryRepository categoryRepository;
    ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {

    }
}
