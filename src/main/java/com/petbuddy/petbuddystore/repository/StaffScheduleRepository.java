package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, String> {
}
