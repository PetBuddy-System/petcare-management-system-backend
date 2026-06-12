package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.Role;
import com.petbuddy.petbuddystore.common.enums.ScheduleStatus;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleReassignRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
    public StaffScheduleResponse createStaffSchedule(StaffScheduleCreationRequest request) {
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (staff.getRole() != Role.STAFF) {
            throw new AppException(ErrorCode.USER_NOT_STAFF);
        }

        boolean hasExistedSchedule = scheduleRepository.existsSchedule(
                staff.getUserId(), request.getWorkDate(), request.getStartTime(),
                request.getEndTime(), null);

        if (hasExistedSchedule) {
            throw new AppException(ErrorCode.SCHEDULE_ALREADY_EXISTS);
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
    public List<StaffScheduleResponse> getMySchedules(LocalDate fromDate, LocalDate toDate) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User staff = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (staff.getRole() != Role.STAFF) {
            throw new AppException(ErrorCode.USER_NOT_STAFF);
        }

        List<StaffSchedule> schedules;
        if (fromDate != null && toDate != null) {
            if(fromDate.isAfter(toDate)) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
            schedules = scheduleRepository.findByStaff_UserIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(userId, fromDate, toDate);
        } else {
            schedules = scheduleRepository.findByStaff_UserIdOrderByWorkDateAscStartTimeAsc(userId);
        }

        return schedules.stream()
                .map(scheduleMapper::toStaffScheduleResponse)
                .toList();
    }

    @Override
    public List<StaffScheduleResponse> getAllStaffSchedules(LocalDate fromDate, LocalDate toDate, LocalDate workDate) {
        List<StaffSchedule> schedules;

        if (workDate != null) {
            schedules = scheduleRepository.findByWorkDateOrderByStartTimeAsc(workDate);
        } else if (fromDate != null && toDate != null) {
            if(fromDate.isAfter(toDate)) {
                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
            }
            schedules = scheduleRepository.findByWorkDateBetweenOrderByWorkDateAscStartTimeAsc(fromDate, toDate);
        } else {
            schedules = scheduleRepository.findAll();
        }

        return schedules.stream()
                .map(scheduleMapper::toStaffScheduleResponse)
                .toList();
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

    @Override
    public StaffScheduleResponse reassignStaff(String scheduleId, StaffScheduleReassignRequest request) {
        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (schedule.getScheduleStatus() != ScheduleStatus.SCHEDULED) {
            throw new AppException(ErrorCode.CANNOT_UPDATE);
        }

        User newStaff = userRepository.findById(request.getNewStaff())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (newStaff.getRole() != Role.STAFF) {
                throw new AppException(ErrorCode.USER_NOT_STAFF);
        }

        if (schedule.getStaff().getUserId().equals(newStaff.getUserId())) {
            throw new AppException(ErrorCode.SAME_STAFF);
        }

        boolean hasExistedSchedule = scheduleRepository.existsSchedule(
                newStaff.getUserId(), schedule.getWorkDate(), schedule.getStartTime(),
                schedule.getEndTime(), schedule.getScheduleId());

        if (hasExistedSchedule) {
            throw new AppException(ErrorCode.SCHEDULE_ALREADY_EXISTS);
        }
        schedule.setStaff(newStaff);
        schedule.setNote(request.getReason());
        return scheduleMapper.toStaffScheduleResponse(scheduleRepository.save(schedule));
    }


}
