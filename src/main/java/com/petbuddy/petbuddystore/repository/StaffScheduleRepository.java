package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, String> {
    boolean existsByStaff_UserIdAndWorkSchedule_WorkScheduleId(String staffId, String workScheduleId);

    @Query("""
        SELECT COUNT(ss) > 0
        FROM StaffSchedule ss
        JOIN ss.workSchedule ws
        WHERE ss.staff.userId = :staffId
          AND ws.workDate = :workDate
          AND (:currentWorkScheduleId IS NULL OR ws.workScheduleId <> :currentWorkScheduleId)
          AND ws.scheduleStatus IN ('SCHEDULED', 'WORKING')
          AND ws.startTime < :endTime
          AND ws.endTime > :startTime
    """)
    boolean existsSchedule(@Param("staffId") String staffId, @Param("workDate") LocalDate workDate,
            @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime,
            @Param("currentWorkScheduleId") String currentWorkScheduleId);
}
