package com.petbuddy.petbuddystore.dto.response;

import com.petbuddy.petbuddystore.common.enums.ScheduleStatus;
import com.petbuddy.petbuddystore.common.enums.ShiftType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffScheduleResponse {
    String scheduleId;
    String staffId;
    String staffName;
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
    String note;
    ShiftType shiftType;
    ScheduleStatus scheduleStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
