package com.smartgovernance.repository;

import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.Escalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationRepository extends JpaRepository<Escalation, Long> {
    List<Escalation> findByComplaintOrderByCreatedAtDesc(Complaint complaint);
    List<Escalation> findByStatusOrderByCreatedAtDesc(String status);
}
