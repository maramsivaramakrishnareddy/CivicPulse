package com.smartgovernance.repository;

import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.ReopenRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReopenRequestRepository extends JpaRepository<ReopenRequest, Long> {
    List<ReopenRequest> findByComplaintOrderByCreatedAtDesc(Complaint complaint);
}
