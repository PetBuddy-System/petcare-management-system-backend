package com.petbuddy.petbuddystore.mapper;

import com.petbuddy.petbuddystore.dto.request.StaffScheduleCreationRequest;
import com.petbuddy.petbuddystore.dto.request.StaffScheduleUpdateRequest;
import com.petbuddy.petbuddystore.dto.response.StaffScheduleResponse;
import com.petbuddy.petbuddystore.model.StaffSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StaffScheduleMapper {
    StaffSchedule toStaffSchedule(StaffScheduleCreationRequest request);

    @Mapping(source = "staff.userId", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    StaffScheduleResponse toStaffScheduleResponse(StaffSchedule staffSchedule);

    void updateStaffSchedule(@MappingTarget StaffSchedule staffSchedule, StaffScheduleUpdateRequest request);
}
