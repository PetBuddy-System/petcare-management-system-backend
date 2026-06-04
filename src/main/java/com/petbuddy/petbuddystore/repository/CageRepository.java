package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.Cage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CageRepository extends JpaRepository<Cage, Long> {
    boolean existsByCageCode(String cageCode);
}
