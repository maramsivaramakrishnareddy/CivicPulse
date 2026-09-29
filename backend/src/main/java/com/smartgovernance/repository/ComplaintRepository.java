package com.smartgovernance.repository;

import com.smartgovernance.entity.Complaint;
import com.smartgovernance.entity.Citizen;
import com.smartgovernance.entity.Department;
import com.smartgovernance.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {
    @Query("SELECT c FROM Complaint c LEFT JOIN FETCH c.department LEFT JOIN FETCH c.citizen LEFT JOIN FETCH c.officer WHERE c.citizen = :citizen ORDER BY c.createdAt DESC")
    List<Complaint> findByCitizenOrderByCreatedAtDesc(@Param("citizen") Citizen citizen);
    
    List<Complaint> findByDepartmentOrderByCreatedAtDesc(Department department);

    @Query("SELECT c FROM Complaint c LEFT JOIN FETCH c.department LEFT JOIN FETCH c.citizen LEFT JOIN FETCH c.officer WHERE LOWER(TRIM(c.department.name)) = LOWER(TRIM(:departmentName)) ORDER BY c.createdAt DESC")
    List<Complaint> findByDepartmentNameOrderByCreatedAtDesc(@Param("departmentName") String departmentName);
    
    List<Complaint> findByOfficerOrderByCreatedAtDesc(Officer officer);
    
    @Query("SELECT c FROM Complaint c LEFT JOIN FETCH c.department ORDER BY c.createdAt DESC")
    List<Complaint> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT DISTINCT c FROM Complaint c LEFT JOIN FETCH c.department LEFT JOIN FETCH c.citizen LEFT JOIN FETCH c.officer WHERE c.complaintId = :complaintId")
    java.util.Optional<Complaint> findComplaintWithDetails(@Param("complaintId") String complaintId);
    
    List<Complaint> findByStatusOrderByCreatedAtDesc(Complaint.ComplaintStatus status);
}
