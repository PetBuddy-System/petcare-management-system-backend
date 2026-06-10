package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.Role;
import com.petbuddy.petbuddystore.common.enums.ScheduleStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.StaffScheduleResponse;
import com.petbuddy.petbuddystore.mapper.StaffScheduleMapper;
import com.petbuddy.petbuddystore.model.StaffSchedule;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.repository.StaffScheduleRepository;
import com.petbuddy.petbuddystore.repository.UserRepository;
import com.petbuddy.petbuddystore.service.StaffScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StaffScheduleServiceImpl implements StaffScheduleService {
    StaffScheduleMapper scheduleMapper;
    StaffScheduleRepository scheduleRepository;
    UserRepository userRepository;

    @Override
    public StaffScheduleResponse createStaffSchedule(StaffScheduleCreationRequest request, String staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (staff.getRole() != Role.STAFF) {
            throw new AppException(ErrorCode.USER_NOT_STAFF);
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_WORKING_TIME);
        }

        StaffSchedule schedule = scheduleMapper.toStaffSchedule(request);
        schedule.setStaff(staff);
        schedule.setScheduleStatus(ScheduleStatus.SCHEDULED);
        return scheduleMapper.toStaffScheduleResponse(scheduleRepository.save(schedule));
    }

    @Override
    public StaffScheduleResponse getStaffScheduleById(String scheduleId) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        return scheduleMapper.toStaffScheduleResponse(schedule);
    }

    @Override
    public StaffScheduleResponse updateStaffSchedule(String scheduleId, StaffScheduleUpdateRequest request) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getScheduleStatus() != ScheduleStatus.SCHEDULED) {
            throw new AppException(ErrorCode.CANNOT_UPDATE);
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_WORKING_TIME);
        }

        scheduleMapper.updateStaffSchedule(schedule, request);
        return scheduleMapper.toStaffScheduleResponse(scheduleRepository.save(schedule));
    }
}
