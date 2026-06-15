package com.petbuddy.petbuddystore.service.impl;

import com.petbuddy.petbuddystore.common.enums.Role;
import com.petbuddy.petbuddystore.common.exception.AppException;
import com.petbuddy.petbuddystore.common.exception.ErrorCode;
import com.petbuddy.petbuddystore.dto.request.WorkScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.response.WorkScheduleResponse;
import com.petbuddy.petbuddystore.mapper.WorkScheduleMapper;
import com.petbuddy.petbuddystore.model.StaffSchedule;
import com.petbuddy.petbuddystore.model.User;
import com.petbuddy.petbuddystore.model.WorkSchedule;
import com.petbuddy.petbuddystore.repository.StaffScheduleRepository;
import com.petbuddy.petbuddystore.repository.WorkScheduleRepository;
import com.petbuddy.petbuddystore.repository.UserRepository;
import com.petbuddy.petbuddystore.service.WorkScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WorkScheduleServiceImpl implements WorkScheduleService {
    WorkScheduleMapper workScheduleMapper;
    WorkScheduleRepository workScheduleRepository;
    UserRepository userRepository;
    StaffScheduleRepository staffScheduleRepository;

    @Override
    public WorkScheduleResponse createWorkSchedule(WorkScheduleCreationRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_WORKING_TIME);
        }

        WorkSchedule workSchedule = workScheduleMapper.toWorkSchedule(request);
        workSchedule = workScheduleRepository.save(workSchedule);

        if (!request.getStaffIds().isEmpty()){
            assignStaffToSchedule(workSchedule, request.getStaffIds());
        }

        WorkSchedule savedWorkSchedule = workScheduleRepository.findByIdWithStaffs(workSchedule.getWorkScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_EXISTED));

        return workScheduleMapper.toWorkScheduleResponse(savedWorkSchedule);
    }

    private void assignStaffToSchedule(WorkSchedule workSchedule, List<String> staffIds) {
        for (String staffId : staffIds) {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (staff.getRole() != Role.STAFF) {
                throw new AppException(ErrorCode.USER_NOT_STAFF);
            }
            boolean scheduleAssigned = staffScheduleRepository.existsByStaff_UserIdAndWorkSchedule_WorkScheduleId
                    (staffId, workSchedule.getWorkScheduleId());

            if (scheduleAssigned) {
                throw new AppException(ErrorCode.STAFF_ALREADY_ASSIGNED_TO_SCHEDULE);
            }

            boolean hasExistedSchedule = staffScheduleRepository.existsSchedule(
                    staffId, workSchedule.getWorkDate(), workSchedule.getStartTime(),
                    workSchedule.getEndTime(), workSchedule.getWorkScheduleId());

            if (hasExistedSchedule) {
                throw new AppException(ErrorCode.SCHEDULE_ALREADY_EXISTS);
            }

            StaffSchedule staffSchedule = StaffSchedule.builder()
                    .staff(staff)
                    .workSchedule(workSchedule)
                    .assignedAt(LocalDateTime.now())
                    .build();
            staffScheduleRepository.save(staffSchedule);
        }

    }

    @Override
    public WorkScheduleResponse getWorkScheduleById(String workScheduleId) {
        WorkSchedule schedule = workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_EXISTED));
        return workScheduleMapper.toWorkScheduleResponse(schedule);
    }
//
//    @Override
//    public List<WorkScheduleResponse> getMySchedules(LocalDate fromDate, LocalDate toDate) {
//        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
//        User staff = userRepository.findById(userId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        if (staff.getRole() != Role.STAFF) {
//            throw new AppException(ErrorCode.USER_NOT_STAFF);
//        }
//
//        List<StaffSchedule> schedules;
//        if (fromDate != null && toDate != null) {
//            if(fromDate.isAfter(toDate)) {
//                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
//            }
//            schedules = scheduleRepository.findByStaff_UserIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(userId, fromDate, toDate);
//        } else {
//            schedules = scheduleRepository.findByStaff_UserIdOrderByWorkDateAscStartTimeAsc(userId);
//        }
//
//        return schedules.stream()
//                .map(scheduleMapper::toStaffScheduleResponse)
//                .toList();
//    }
//
//    @Override
//    public List<WorkScheduleResponse> getAllStaffSchedules(LocalDate fromDate, LocalDate toDate, LocalDate workDate) {
//        List<StaffSchedule> schedules;
//
//        if (workDate != null) {
//            schedules = scheduleRepository.findByWorkDateOrderByStartTimeAsc(workDate);
//        } else if (fromDate != null && toDate != null) {
//            if(fromDate.isAfter(toDate)) {
//                throw new AppException(ErrorCode.INVALID_DATE_RANGE);
//            }
//            schedules = scheduleRepository.findByWorkDateBetweenOrderByWorkDateAscStartTimeAsc(fromDate, toDate);
//        } else {
//            schedules = scheduleRepository.findAll();
//        }
//
//        return schedules.stream()
//                .map(scheduleMapper::toStaffScheduleResponse)
//                .toList();
//    }
//
//    @Override
//    public WorkScheduleResponse updateStaffSchedule(String scheduleId, WorkScheduleUpdateRequest request) {
//        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
//                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
//
//        if (schedule.getScheduleStatus() != ScheduleStatus.SCHEDULED) {
//            throw new AppException(ErrorCode.CANNOT_UPDATE);
//        }
//
//        if (!request.getStartTime().isBefore(request.getEndTime())) {
//            throw new AppException(ErrorCode.INVALID_WORKING_TIME);
//        }
//
//        scheduleMapper.updateStaffSchedule(schedule, request);
//        return scheduleMapper.toStaffScheduleResponse(scheduleRepository.save(schedule));
//    }

//    @Override
//    public WorkScheduleResponse reassignStaff(String scheduleId, StaffScheduleReassignRequest request) {
//        StaffSchedule schedule = scheduleRepository.findById(scheduleId)
//                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
//
//        if (schedule.getScheduleStatus() != ScheduleStatus.SCHEDULED) {
//            throw new AppException(ErrorCode.CANNOT_UPDATE);
//        }
//
//        User newStaff = userRepository.findById(request.getNewStaff())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        if (newStaff.getRole() != Role.STAFF) {
//                throw new AppException(ErrorCode.USER_NOT_STAFF);
//        }
//
//        if (schedule.getStaff().getUserId().equals(newStaff.getUserId())) {
//            throw new AppException(ErrorCode.SAME_STAFF);
//        }
//
//        boolean hasExistedSchedule = scheduleRepository.existsSchedule(
//                newStaff.getUserId(), schedule.getWorkDate(), schedule.getStartTime(),
//                schedule.getEndTime(), schedule.getScheduleId());
//
//        if (hasExistedSchedule) {
//            throw new AppException(ErrorCode.SCHEDULE_ALREADY_EXISTS);
//        }
//        schedule.setStaff(newStaff);
//        schedule.setNote(request.getReason());
//        return scheduleMapper.toStaffScheduleResponse(scheduleRepository.save(schedule));
//    }


}
