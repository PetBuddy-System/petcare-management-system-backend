package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.StaffScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleReassignRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.StaffScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface StaffScheduleService {
    StaffScheduleResponse createStaffSchedule(StaffScheduleCreationRequest request);
    StaffScheduleResponse getStaffScheduleById(String scheduleId);
    StaffScheduleResponse reassignStaff(String scheduleId, StaffScheduleReassignRequest request);
    StaffScheduleResponse updateStaffSchedule(String scheduleId, StaffScheduleUpdateRequest request);
    List<StaffScheduleResponse> getMySchedules(LocalDate fromDate, LocalDate toDate);
    List<StaffScheduleResponse> getAllStaffSchedules(LocalDate fromDate, LocalDate toDate, LocalDate workDate);
}
