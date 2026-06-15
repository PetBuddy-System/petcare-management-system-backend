package com.petbuddy.petbuddystore.controller;

import com.petbuddy.petbuddystore.common.response.ApiResponse;
import com.petbuddy.petbuddystore.dto.request.WorkScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleReassignRequest;
import com.petbuddy.petbuddystore.dto.request.WorkScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.WorkScheduleResponse;
import com.petbuddy.petbuddystore.service.WorkScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/work-schedules")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Work Schedule API", description = "Quản lí lịch làm việc của Staff (CRUD SCHEDULE)")
public class WorkScheduleController {
    WorkScheduleService workScheduleService;

    @PostMapping()
    @Operation(description = "Tạo mới lịch làm việc")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> createWorkSchedule(@RequestBody @Valid WorkScheduleCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Work schedule created successfully", workScheduleService.createWorkSchedule(request)));
    }

    @GetMapping("/{workScheduleId}")
    @Operation(description = "Lấy thông tin chi tiết lịch làm việc theo id")
    public ResponseEntity<ApiResponse<WorkScheduleResponse>> getWorkScheduleById(@PathVariable String workScheduleId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(workScheduleService.getWorkScheduleById(workScheduleId)));
    }
//
//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> getMySchedules(@RequestParam(required = false) LocalDate fromDate,
//                                                                                  @RequestParam(required = false) LocalDate toDate) {
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiResponse.success(scheduleService.getMySchedules(fromDate, toDate)));
//    }
//
//    @GetMapping()
//    public ResponseEntity<ApiResponse<List<WorkScheduleResponse>>> getAllStaffSchedules(@RequestParam(required = false) LocalDate fromDate,
//                                                                                        @RequestParam(required = false) LocalDate toDate,
//                                                                                        @RequestParam(required = false) LocalDate workDate) {
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiResponse.success(scheduleService.getAllStaffSchedules(fromDate, toDate, workDate)));
//    }
//
//    @PutMapping("/{scheduleId}")
//    @Operation(description = "Update thông tin schedule theo id")
//    public ResponseEntity<ApiResponse<WorkScheduleResponse>> updateStaffSchedule(@PathVariable String scheduleId, @RequestBody @Valid WorkScheduleUpdateRequest request){
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiResponse.success("Staff schedule updated successfully",scheduleService.updateStaffSchedule(scheduleId, request)));
//    }
//
//    @PatchMapping("/{scheduleId}/reassign-staff")
//    public ResponseEntity<ApiResponse<WorkScheduleResponse>> reassignStaff(@PathVariable String scheduleId,
//                                                                           @RequestBody StaffScheduleReassignRequest request) {
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiResponse.success("Staff schedule updated successfully",scheduleService.reassignStaff(scheduleId, request)));
//    }
}
