package com.petbuddy.petbuddystore.configuration;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvLoader {
    public static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}
