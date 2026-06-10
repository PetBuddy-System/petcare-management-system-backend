package com.petbuddy.petbuddystore.service;

import com.petbuddy.petbuddystore.dto.request.StaffScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.StaffScheduleResponse;

public interface StaffScheduleService {
    StaffScheduleResponse createStaffSchedule(StaffScheduleCreationRequest request, String staffId);

    StaffScheduleResponse getStaffScheduleById(String scheduleId);
    StaffScheduleResponse updateStaffSchedule(String scheduleId, StaffScheduleUpdateRequest request);

}
