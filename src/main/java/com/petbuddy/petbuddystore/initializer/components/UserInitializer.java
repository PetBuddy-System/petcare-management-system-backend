package com.petbuddy.petbuddystore.initializer.components;

import com.petbuddy.petbuddystore.common.enums.Role;
import com.petbuddy.petbuddystore.common.enums.UserStatus;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void init() {

        List<User> users = List.of(
                buildUser("user1@gmail.com", "Nguyen Van A", "Male", LocalDate.of(2003, 1, 1), Role.CUSTOMER),
                buildUser("user2@gmail.com", "Tran Thi B", "Female", LocalDate.of(2002, 5, 10), Role.CUSTOMER),
                buildUser("user3@gmail.com", "Le Van C", "Male", LocalDate.of(2001, 8, 20), Role.CUSTOMER),
                buildUser("manager@gmail.com", "Manager Test", "Male", LocalDate.of(1998, 3, 15), Role.MANAGER),
                buildUser("admin@gmail.com", "Admin Test", "Female", LocalDate.of(1995, 7, 7), Role.ADMIN)
        );

        System.out.println("========== USERS INITIALIZER ==========");

        users.forEach(user -> {
            if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
                User savedUser = userRepository.save(user);

                System.out.println(
                        "Created: " + savedUser.getEmail()
                                + " | User ID: " + savedUser.getUserId()
                                + " | Password: 12345678"
                );
            } else {
                User existingUser = userRepository.findByEmail(user.getEmail()).get();

                System.out.println(
                        "Existed: " + existingUser.getEmail()
                                + " | User ID: " + existingUser.getUserId()
                );
            }
        });
    }

    private User buildUser(String email, String fullName, String gender, LocalDate dateOfBirth, Role role) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode("12345678"))
                .fullName(fullName)
                .gender(gender)
                .dateOfBirth(dateOfBirth)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }
}