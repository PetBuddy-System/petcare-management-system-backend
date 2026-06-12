package com.petbuddy.petbuddystore.dto.request;

import com.petbuddy.petbuddystore.common.enums.ScheduleStatus;
import com.petbuddy.petbuddystore.common.enums.ShiftType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffScheduleCreationRequest {
    String staffId;
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
    String note;
    ShiftType shiftType;
    ScheduleStatus scheduleStatus;
}
