package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleReassignRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.StaffScheduleResponse;
import com.petbuddy.petbuddystore.service.StaffScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff-schedules")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Staff Schedule API", description = "Quản lí lịch làm việc của Staff")
public class StaffScheduleController {
    StaffScheduleService scheduleService;

    @PostMapping()
    @Operation(description = "Tạo mới lịch làm việc")
    public ResponseEntity<ApiResponse<StaffScheduleResponse>> createStaffSchedule(@RequestBody @Valid StaffScheduleCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff schedule created successfully", scheduleService.createStaffSchedule(request)));
    }

    @GetMapping("/{scheduleId}")
    @Operation(description = "Lấy thông tin chi tiết lịch làm việc theo id")
    public ResponseEntity<ApiResponse<StaffScheduleResponse>> getStaffScheduleById(@PathVariable String scheduleId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(scheduleService.getStaffScheduleById(scheduleId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<StaffScheduleResponse>>> getMySchedules(@RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(scheduleService.getMySchedules(fromDate, toDate)));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<StaffScheduleResponse>>> getAllStaffSchedules(@RequestParam(required = false) LocalDate fromDate,
                                                                                         @RequestParam(required = false) LocalDate toDate,
                                                                                         @RequestParam(required = false) LocalDate workDate) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(scheduleService.getAllStaffSchedules(fromDate, toDate, workDate)));
    }

    @PutMapping("/{scheduleId}")
    @Operation(description = "Update thông tin schedule theo id")
    public ResponseEntity<ApiResponse<StaffScheduleResponse>> updateStaffSchedule(@PathVariable String scheduleId, @RequestBody @Valid StaffScheduleUpdateRequest request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Staff schedule updated successfully",scheduleService.updateStaffSchedule(scheduleId, request)));
    }

    @PatchMapping("/{scheduleId}/reassign-staff")
    public ResponseEntity<ApiResponse<StaffScheduleResponse>> reassignStaff(@PathVariable String scheduleId,
                                                            @RequestBody StaffScheduleReassignRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("Staff schedule updated successfully",scheduleService.reassignStaff(scheduleId, request)));
    }
}
