package com.smartgovernance.repository;

import com.smartgovernance.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for Citizen entity.
 */
@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByEmail(String email);

    Optional<Citizen> findByAadhaarNumber(String aadhaarNumber);

    boolean existsByEmail(String email);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    boolean existsByPhoneNumber(String phoneNumber);
}
