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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Officer Module seeder.
 *
 * Guarantees every officer has at least MIN_ASSIGNED_COMPLAINTS complaints for which
 * Complaint.officer == that officer, using the existing officer/complaint relationship
 * (no schema change, no mock/hard-coded frontend data). Runs once on every startup,
 * topping up any officer that falls short, and never touches or deletes existing
 * Citizen/Admin data or existing complaints.
 *
 * Uses ApplicationReadyEvent (same lifecycle hook as the existing SchemaMigration
 * component) so it always runs after the pre-existing DepartmentDataInitializer and
 * DataInitializer (officer seeding) CommandLineRunners have already populated
 * departments and officers - without needing to modify either of those files.
 */
@Component
public class OfficerComplaintSeeder {

    private static final Logger logger = LoggerFactory.getLogger(OfficerComplaintSeeder.class);
    private static final int MIN_ASSIGNED_COMPLAINTS = 5;

    private static final String SEED_CITIZEN_EMAIL = "seed.citizen@smartgov.local";
    private static final String SEED_CITIZEN_AADHAAR = "999999999999";

    private static final List<String> SAMPLE_TITLES = List.of(
            "Overflowing garbage bin on main road",
            "Broken streetlight near junction",
            "Water supply disruption in locality",
            "Pothole causing traffic hazard",
            "Illegal garbage dumping near park",
            "Leaking water pipeline on street",
            "Damaged road divider",
            "Frequent power outages in area",
            "Blocked stormwater drain",
            "Unsafe electrical wiring on pole"
    );

    private final ComplaintRepository complaintRepository;
    private final OfficerRepository officerRepository;
    private final DepartmentRepository departmentRepository;
    private final CitizenRepository citizenRepository;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public OfficerComplaintSeeder(ComplaintRepository complaintRepository,
                                   OfficerRepository officerRepository,
                                   DepartmentRepository departmentRepository,
                                   CitizenRepository citizenRepository,
                                   ComplaintHistoryRepository complaintHistoryRepository,
                                   PasswordEncoder passwordEncoder) {
        this.complaintRepository = complaintRepository;
        this.officerRepository = officerRepository;
        this.departmentRepository = departmentRepository;
        this.citizenRepository = citizenRepository;
        this.complaintHistoryRepository = complaintHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void ensureEveryOfficerHasAssignedComplaints() {
        try {
            List<Officer> officers = officerRepository.findAll();
            if (officers.isEmpty()) {
                logger.info("Officer Module seeder: no officers found yet, skipping complaint seeding.");
                return;
            }

            Citizen seedCitizen = getOrCreateSeedCitizen();
            // Realistic distribution across the officer dashboard's 4 buckets.
            Complaint.ComplaintStatus[] statusCycle = {
                    Complaint.ComplaintStatus.APPLIED,
                    Complaint.ComplaintStatus.ACTION_TO_BE_TAKEN,
                    Complaint.ComplaintStatus.COMPLAINT_RESOLVED,
                    Complaint.ComplaintStatus.REJECTED,
                    Complaint.ComplaintStatus.COMPLAINT_VIEWED
            };

            AtomicLong idSuffix = new AtomicLong(System.currentTimeMillis());

            for (Officer officer : officers) {
                long existing = complaintRepository.findByOfficerOrderByCreatedAtDesc(officer).size();
                if (existing >= MIN_ASSIGNED_COMPLAINTS) {
                    continue;
                }

                Department department = departmentRepository.findByName(officer.getDepartment())
                        .orElseGet(() -> departmentRepository.save(
                                new Department(officer.getDepartment(), "Auto-created department")));

                long toCreate = MIN_ASSIGNED_COMPLAINTS - existing;
                for (int i = 0; i < toCreate; i++) {
                    Complaint.ComplaintStatus status = statusCycle[i % statusCycle.length];
                    Complaint complaint = new Complaint();
                    complaint.setComplaintId("CMP-SEED-" + officer.getOfficerId() + "-" + idSuffix.incrementAndGet());
                    complaint.setCitizen(seedCitizen);
                    complaint.setDepartment(department);
                    complaint.setOfficer(officer);
                    complaint.setTitle(SAMPLE_TITLES.get((int) ((existing + i) % SAMPLE_TITLES.size())));
                    complaint.setDescription("Seeded complaint assigned to " + officer.getFullName()
                            + " (" + officer.getDepartment() + ") for Officer Module demonstration purposes.");
                    complaint.setLocation(officer.getDepartment() + " Zone, Ward " + ((i % 9) + 1));
                    complaint.setComplaintType(Complaint.ComplaintType.NORMAL);
                    complaint.setStatus(status);
                    complaint.setDuplicateStatus(Complaint.DuplicateStatus.NOT_DUPLICATE);
                    if (status == Complaint.ComplaintStatus.COMPLAINT_RESOLVED) {
                        complaint.setActionTaken("Issue resolved by officer.");
                        complaint.setResolvedAt(LocalDateTime.now());
                    } else if (status == Complaint.ComplaintStatus.REJECTED) {
                        complaint.setActionTaken("Complaint rejected: not a valid grievance for this department.");
                    }

                    Complaint saved = complaintRepository.save(complaint);

                    ComplaintHistory history = new ComplaintHistory(saved, saved.getStatus(),
                            "Complaint assigned and set to " + status + " (seed data).");
                    history.setOfficer(officer);
                    complaintHistoryRepository.save(history);
                }

                logger.info("Officer Module seeder: assigned {} new complaint(s) to officer {} ({}).",
                        toCreate, officer.getEmployeeId(), officer.getFullName());
            }
        } catch (Exception e) {
            // Never block application startup because of seed data issues.
            logger.error("Officer Module seeder encountered an error: {}", e.getMessage(), e);
        }
    }

    private Citizen getOrCreateSeedCitizen() {
        return citizenRepository.findByEmail(SEED_CITIZEN_EMAIL)
                .orElseGet(() -> {
                    Citizen citizen = new Citizen(
                            "Seed Data Citizen",
                            SEED_CITIZEN_AADHAAR,
                            SEED_CITIZEN_EMAIL,
                            "9999999999",
                            passwordEncoder.encode("SeedCitizen@123"),
                            "Officer Module seed data - not a real citizen"
                    );
                    Citizen saved = citizenRepository.save(citizen);
                    logger.info("Officer Module seeder: created dedicated seed citizen for demo complaints.");
                    return saved;
                });
    }
}
