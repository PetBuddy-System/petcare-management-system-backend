package com.petbuddy.petbuddystore.repository;

import com.petbuddy.petbuddystore.model.StaffSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, String> {
    @Query("""
    SELECT COUNT(ss) > 0
    FROM StaffSchedule ss
    WHERE ss.staff.userId = :staffId
      AND ss.workDate = :workDate
      AND (:currentScheduleId IS NULL OR ss.scheduleId <> :currentScheduleId)
      AND ss.scheduleStatus IN ('SCHEDULED', 'WORKING')
      AND ss.startTime < :endTime
      AND ss.endTime > :startTime
""")
    boolean existsSchedule(String staffId, LocalDate workDate, LocalTime startTime, LocalTime endTime,
                                      String currentScheduleId);

    List<StaffSchedule> findByStaff_UserIdOrderByWorkDateAscStartTimeAsc(String staffId);
    List<StaffSchedule> findByStaff_UserIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(String staffId, LocalDate fromDate, LocalDate toDate);

    List<StaffSchedule> findByWorkDateOrderByStartTimeAsc(LocalDate workDate);
    List<StaffSchedule> findByWorkDateBetweenOrderByWorkDateAscStartTimeAsc(LocalDate fromDate, LocalDate toDate);
}
