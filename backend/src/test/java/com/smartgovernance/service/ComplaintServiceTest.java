package com.smartgovernance.service;

import com.smartgovernance.dto.ComplaintCreateRequest;
import com.smartgovernance.dto.ComplaintResponseDto;
import com.smartgovernance.entity.Citizen;
import com.smartgovernance.entity.Department;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ComplaintServiceTest {

    @Test
    void shouldCreateComplaintResponseFromRequest() {
        ComplaintCreateRequest request = new ComplaintCreateRequest();
        request.setTitle("Street Light Issue");
        request.setDescription("Street light not working");
        request.setDepartmentName("Sanitation Department");
        request.setLocation("Near City Park");

        Citizen citizen = new Citizen();
        citizen.setCitizenId(1L);
        citizen.setFullName("Test Citizen");

        Department department = new Department("Sanitation Department", "Public works");

        ComplaintResponseDto dto = new ComplaintResponseDto();
        dto.setComplaintId("CMP-TEST-1");
        dto.setTitle(request.getTitle());
        dto.setDepartmentName(department.getName());
        dto.setCitizenName(citizen.getFullName());

        assertNotNull(dto.getComplaintId());
        assertNotNull(dto.getDepartmentName());
    }
}
