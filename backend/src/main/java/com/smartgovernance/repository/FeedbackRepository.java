package com.smartgovernance.repository;

import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByComplaintOrderByCreatedAtDesc(Complaint complaint);
    Optional<Feedback> findFirstByComplaintOrderByCreatedAtDesc(Complaint complaint);
    Optional<Feedback> findByComplaint(Complaint complaint);
    boolean existsByComplaint(Complaint complaint);
}
