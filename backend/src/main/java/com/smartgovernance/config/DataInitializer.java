package com.smartgovernance.config;

import com.smartgovernance.entity.Officer;
import com.smartgovernance.repository.OfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Data Initializer running on application startup.
 * Seeds development officer accounts for each supported department if not already present.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(OfficerRepository officerRepository, PasswordEncoder passwordEncoder) {
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        List<OfficerSeed> officers = List.of(
                new OfficerSeed("Test Officer", "EMP1001", "Sanitation Department", "officer@smartgov.local", "9876543210", "Officer@123"),
                new OfficerSeed("Water Officer", "EMP1002", "Water Supply Department", "water.officer@smartgov.local", "9876543211", "Water@123"),
                new OfficerSeed("Road Officer", "EMP1003", "Road Maintenance Department", "road.officer@smartgov.local", "9876543212", "Road@123"),
                new OfficerSeed("Health Officer", "EMP1004", "Public Health Department", "health.officer@smartgov.local", "9876543213", "Health@123"),
                new OfficerSeed("Power Officer", "EMP1005", "Electricity Department", "power.officer@smartgov.local", "9876543214", "Power@123"),
                new OfficerSeed("Traffic Officer", "EMP1006", "Traffic & Transport Department", "traffic.officer@smartgov.local", "9876543215", "Traffic@123"),
                new OfficerSeed("Parks Officer", "EMP1007", "Parks & Horticulture Department", "parks.officer@smartgov.local", "9876543216", "Parks@123"),
                new OfficerSeed("Safety Officer", "EMP1008", "Public Safety Department", "safety.officer@smartgov.local", "9876543217", "Safety@123"),
                new OfficerSeed("Drainage Officer", "EMP1009", "Drainage & Sewerage Department", "drainage.officer@smartgov.local", "9876543218", "Drainage@123"),
                new OfficerSeed("Lighting Officer", "EMP1010", "Street Lighting Department", "lighting.officer@smartgov.local", "9876543219", "Lighting@123"),
                new OfficerSeed("Building Officer", "EMP1011", "Building & Construction Department", "building.officer@smartgov.local", "9876543220", "Building@123"),
                new OfficerSeed("Animal Control Officer", "EMP1012", "Animal Control Department", "animal.officer@smartgov.local", "9876543221", "Animal@123")
        );

        for (OfficerSeed seed : officers) {
            if (!officerRepository.existsByEmployeeId(seed.employeeId())) {
                officerRepository.save(new Officer(
                        seed.fullName(), seed.employeeId(), seed.department(), seed.email(),
                        seed.phoneNumber(), passwordEncoder.encode(seed.password())
                ));
                logger.info("Default officer created for {} with Employee ID: {}", seed.department(), seed.employeeId());
            }
        }
    }

    private record OfficerSeed(String fullName, String employeeId, String department,
                               String email, String phoneNumber, String password) {
    }
}
