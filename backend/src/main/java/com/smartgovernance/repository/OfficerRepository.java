package com.smartgovernance.repository;

import com.smartgovernance.entity.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Officer entity.
 */
@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {

    Optional<Officer> findByEmployeeId(String employeeId);

    Optional<Officer> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
