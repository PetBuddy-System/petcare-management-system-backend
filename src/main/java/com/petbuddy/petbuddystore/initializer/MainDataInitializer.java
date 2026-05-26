package com.petbuddy.petbuddystore.initializer;

import com.petbuddy.petbuddystore.initializer.components.CategoryInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainDataInitializer implements CommandLineRunner {

    private final CategoryInitializer categoryInitializer;

    @Override
    public void run(String... args) {
        categoryInitializer.init();
    }
}