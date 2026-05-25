package com.petbuddy.petbuddystore;

import com.petbuddy.petbuddystore.configuration.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PetbuddyStoreApplication {

    public static void main(String[] args) {
        DotenvLoader.loadEnv();
        SpringApplication.run(PetbuddyStoreApplication.class, args);
    }

}
