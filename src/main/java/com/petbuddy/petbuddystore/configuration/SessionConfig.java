package com.petbuddy.petbuddystore.configuration;

import com.petbuddy.petbuddystore.session.CartSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class SessionConfig {

    @Bean
    @SessionScope
    public CartSession cartSession() {

        return CartSession.builder()
                .build();
    }
}
