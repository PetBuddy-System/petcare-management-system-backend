package com.petbuddy.petbuddystore.initializer;

import com.petbuddy.petbuddystore.initializer.components.CategoryInitializer;
import com.petbuddy.petbuddystore.initializer.components.ProductInitializer;
import com.petbuddy.petbuddystore.initializer.components.UserInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainDataInitializer implements CommandLineRunner {

    private final CategoryInitializer categoryInitializer;
    private final ProductInitializer productInitializer;
    private final UserInitializer userInitializer;

    @Override
    public void run(String... args) {

        categoryInitializer.init();
        productInitializer.init();
        userInitializer.init();
    }
}