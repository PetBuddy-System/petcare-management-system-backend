package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.common.enums.Role;
import com.petbuddy.petbuddystore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findAllByRole(Role role);
}
