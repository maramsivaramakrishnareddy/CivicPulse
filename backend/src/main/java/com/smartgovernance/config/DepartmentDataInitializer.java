package com.smartgovernance.config;

import com.smartgovernance.entity.Department;
import com.smartgovernance.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DepartmentDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentDataInitializer.class);

    private final DepartmentRepository departmentRepository;

    public DepartmentDataInitializer(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) {
        String[] departments = {
            "Sanitation Department",
            "Water Supply Department",
            "Road Maintenance Department",
            "Public Health Department",
            "Electricity Department",
            "Traffic & Transport Department",
            "Parks & Horticulture Department",
            "Public Safety Department",
            "Drainage & Sewerage Department",
            "Street Lighting Department",
            "Building & Construction Department",
            "Animal Control Department"
        };

        for (String departmentName : departments) {
            if (!departmentRepository.existsByName(departmentName)) {
                departmentRepository.save(new Department(departmentName, "Auto seeded governance department"));
                logger.info("Seeded department: {}", departmentName);
            }
        }
    }
}
