package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.WorkScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleReassignRequest;
import com.petbuddy.petbuddystore.dto.request.WorkScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.WorkScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface WorkScheduleService {
    WorkScheduleResponse createWorkSchedule(WorkScheduleCreationRequest request);
    WorkScheduleResponse getWorkScheduleById(String workScheduleId);
//    WorkScheduleResponse reassignStaff(String scheduleId, StaffScheduleReassignRequest request);
//    WorkScheduleResponse updateStaffSchedule(String workScheduleId, WorkScheduleUpdateRequest request);
//    List<WorkScheduleResponse> getMySchedules(LocalDate fromDate, LocalDate toDate);
//    List<WorkScheduleResponse> getAllStaffSchedules(LocalDate fromDate, LocalDate toDate, LocalDate workDate);
}
