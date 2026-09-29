package com.smartgovernance.config;

import com.smartgovernance.entity.Citizen;
import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.ComplaintHistory;
import com.smartgovernance.entity.Department;
import com.smartgovernance.entity.Officer;
import com.smartgovernance.repository.CitizenRepository;
import com.smartgovernance.repository.ComplaintHistoryRepository;
import com.smartgovernance.repository.ComplaintRepository;
import com.smartgovernance.repository.DepartmentRepository;
import com.smartgovernance.repository.OfficerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creates a small, idempotent built-in demonstration scenario using the same
 * Citizen and Complaint tables used by the real application. The two similar
 * electricity complaints are intentionally left as normal complaints so the
 * existing officer duplicate-detection workflow can discover and verify them.
 */
@Component
@Order(3)
public class DemoCitizenDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DemoCitizenDataInitializer.class);
    private long demoSequence = 0;

    private final CitizenRepository citizenRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintHistoryRepository historyRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoCitizenDataInitializer(CitizenRepository citizenRepository,
                                      ComplaintRepository complaintRepository,
                                      ComplaintHistoryRepository historyRepository,
                                      DepartmentRepository departmentRepository,
                                      OfficerRepository officerRepository,
                                      PasswordEncoder passwordEncoder) {
        this.citizenRepository = citizenRepository;
        this.complaintRepository = complaintRepository;
        this.historyRepository = historyRepository;
        this.departmentRepository = departmentRepository;
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!Boolean.parseBoolean(System.getProperty("app.demo-data.enabled", "true"))) {
            return;
        }

        List<DemoCitizen> citizens = List.of(
                new DemoCitizen("Arjun Kumar", "900000000001", "citizen1@smartgov.demo", "9000000001", "12 Lake View Road"),
                new DemoCitizen("Priya Sharma", "900000000002", "citizen2@smartgov.demo", "9000000002", "24 Market Street"),
                new DemoCitizen("Rahul Verma", "900000000003", "citizen3@smartgov.demo", "9000000003", "8 Green Park"),
                new DemoCitizen("Sneha Reddy", "900000000004", "citizen4@smartgov.demo", "9000000004", "17 School Road"),
                new DemoCitizen("Kiran Rao", "900000000005", "citizen5@smartgov.demo", "9000000005", "31 Temple Lane"),
                new DemoCitizen("Ananya Singh", "900000000006", "citizen6@smartgov.demo", "9000000006", "42 Main Road")
        );

        if (citizenRepository.existsByEmail(citizens.get(0).email())) {
            return; // Already seeded; never recreate or modify existing records.
        }

        for (DemoCitizen seed : citizens) {
            if (!citizenRepository.existsByEmail(seed.email())) {
                citizenRepository.save(new Citizen(
                        seed.name(), seed.aadhaar(), seed.email(), seed.phone(),
                        passwordEncoder.encode("Citizen@123"), seed.address()));
            }
        }

        createComplaint(citizens.get(0).email(), "Sanitation Department",
                "Garbage collection missed in residential area",
                "Household waste has not been collected for several days on Lake View Road.",
                "12 Lake View Road", 16.5088, 80.6480, Complaint.ComplaintStatus.APPLIED);

        createComplaint(citizens.get(1).email(), "Water Supply Department",
                "Low water pressure in Market Street",
                "Water pressure has been unusually low since yesterday and nearby homes are affected.",
                "24 Market Street", 16.5072, 80.6421, Complaint.ComplaintStatus.COMPLAINT_VIEWED);

        createComplaint(citizens.get(2).email(), "Road Maintenance Department",
                "Large pothole near Green Park",
                "A large pothole has formed near the park entrance and is creating a road safety problem.",
                "8 Green Park", 16.5101, 80.6464, Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN);

        createComplaint(citizens.get(3).email(), "Public Health Department",
                "Mosquito breeding near school drain",
                "Stagnant water near the school drain is attracting mosquitoes and needs inspection.",
                "17 School Road", 16.5059, 80.6492, Complaint.ComplaintStatus.APPLIED);

        createComplaint(citizens.get(4).email(), "Sanitation Department",
                "Drainage overflow near Temple Lane",
                "The roadside drainage channel is overflowing after rain and affecting pedestrians.",
                "31 Temple Lane", 16.5092, 80.6510, Complaint.ComplaintStatus.REOPENED);

        // Deliberately similar normal complaint. It is NOT marked DUPLICATE here.
        // The officer duplicate workflow should discover it and the officer confirms it.
        createComplaint(citizens.get(5).email(), "Electricity Department",
                "Street light not working on Main Road",
                "The street light near Main Road is not working at night, making the area difficult to use safely.",
                "42 Main Road", 16.5120, 80.6450, Complaint.ComplaintStatus.APPLIED);

        // Add the matching original after the first electricity complaint is created
        // only when it does not already exist; this keeps both records normal.
        createComplaint(citizens.get(1).email(), "Electricity Department",
                "Streetlight not working near Main Road",
                "A street light near Main Road has stopped working and the same area becomes dark at night.",
                "42 Main Road", 16.5121, 80.6451, Complaint.ComplaintStatus.APPLIED);

        logger.info("Built-in citizen demonstration scenario created: 6 citizens and 7 real Complaint records. Duplicate pair remains unverified for officer workflow.");
    }

    private void createComplaint(String citizenEmail, String departmentName, String title, String description,
                                 String location, double latitude, double longitude, Complaint.ComplaintStatus status) {
        Citizen citizen = citizenRepository.findByEmail(citizenEmail).orElseThrow();
        Department department = departmentRepository.findByName(departmentName).orElseThrow();
        Officer officer = officerRepository.findAll().stream()
                .filter(o -> departmentName.equalsIgnoreCase(o.getDepartment()))
                .findFirst().orElse(null);

        Complaint complaint = new Complaint();
        complaint.setComplaintId("CMP-" + System.currentTimeMillis() + "-" + (++demoSequence));
        complaint.setCitizen(citizen);
        complaint.setDepartment(department);
        complaint.setOfficer(officer);
        complaint.setComplaintType(Complaint.ComplaintType.NORMAL);
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setLocation(location);
        complaint.setLatitude(latitude);
        complaint.setLongitude(longitude);
        complaint.setStatus(status);
        complaint.setDuplicateStatus(Complaint.DuplicateStatus.PENDING_REVIEW);
        Complaint saved = complaintRepository.save(complaint);
        historyRepository.save(new ComplaintHistory(saved, status, "Complaint submitted by citizen."));
    }

    private record DemoCitizen(String name, String aadhaar, String email, String phone, String address) {}
}
