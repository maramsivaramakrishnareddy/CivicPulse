package com.smartgovernance.service.impl;

import com.smartgovernance.dto.*;
import com.smartgovernance.entity.Citizen;
import com.smartgovernance.entity.Officer;
import com.smartgovernance.repository.CitizenRepository;
import com.smartgovernance.repository.OfficerRepository;
import com.smartgovernance.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of AuthService for managing Citizen and Officer authentication.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final CitizenRepository citizenRepository;
    private final OfficerRepository officerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(CitizenRepository citizenRepository,
                           OfficerRepository officerRepository,
                           PasswordEncoder passwordEncoder) {
        this.citizenRepository = citizenRepository;
        this.officerRepository = officerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CitizenResponseDto registerCitizen(CitizenRegisterRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match. Please re-enter your password.");
        }

        // Validate Aadhaar uniqueness
        String sanitizedAadhaar = request.getAadhaarNumber().trim();
        if (citizenRepository.existsByAadhaarNumber(sanitizedAadhaar)) {
            throw new IllegalArgumentException("A citizen account with this Aadhaar number already exists.");
        }

        // Validate Email uniqueness
        String sanitizedEmail = request.getEmail().trim().toLowerCase();
        if (citizenRepository.existsByEmail(sanitizedEmail)) {
            throw new IllegalArgumentException("A citizen account with this email address already exists.");
        }

        // Hash password securely with BCrypt
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Citizen citizen = new Citizen(
                request.getFullName().trim(),
                sanitizedAadhaar,
                sanitizedEmail,
                request.getPhoneNumber().trim(),
                hashedPassword,
                request.getAddress() != null ? request.getAddress().trim() : null
        );

        Citizen saved = citizenRepository.save(citizen);

        return new CitizenResponseDto(
                saved.getCitizenId(),
                saved.getFullName(),
                saved.getAadhaarNumber(),
                saved.getEmail(),
                saved.getPhoneNumber(),
                saved.getAddress(),
                saved.getCreatedAt()
        );
    }

    @Override
    public CitizenResponseDto loginCitizen(CitizenLoginRequest request, HttpSession session) {
        String email = request.getEmail().trim().toLowerCase();
        Optional<Citizen> citizenOpt = citizenRepository.findByEmail(email);

        if (citizenOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), citizenOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Citizen citizen = citizenOpt.get();

        // Establish HTTP Session
        session.setAttribute("userType", "CITIZEN");
        session.setAttribute("userId", citizen.getCitizenId());
        session.setAttribute("userName", citizen.getFullName());

        return new CitizenResponseDto(
                citizen.getCitizenId(),
                citizen.getFullName(),
                citizen.getAadhaarNumber(),
                citizen.getEmail(),
                citizen.getPhoneNumber(),
                citizen.getAddress(),
                citizen.getCreatedAt()
        );
    }

    @Override
    public OfficerResponseDto loginOfficer(OfficerLoginRequest request, HttpSession session) {
        String employeeId = request.getEmployeeId().trim().toUpperCase();
        Optional<Officer> officerOpt = officerRepository.findByEmployeeId(employeeId);

        if (officerOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), officerOpt.get().getPassword())) {
            throw new IllegalArgumentException("Invalid Employee ID or password");
        }

        Officer officer = officerOpt.get();

        // Establish HTTP Session
        session.setAttribute("userType", "OFFICER");
        session.setAttribute("userId", officer.getOfficerId());
        session.setAttribute("userName", officer.getFullName());

        return new OfficerResponseDto(
                officer.getOfficerId(),
                officer.getFullName(),
                officer.getEmployeeId(),
                officer.getDepartment(),
                officer.getEmail(),
                officer.getPhoneNumber(),
                officer.getCreatedAt()
        );
    }

    @Override
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    @Override
    public SessionInfoDto getSessionInfo(HttpSession session) {
        if (session == null || session.getAttribute("userId") == null) {
            return SessionInfoDto.unauthenticated();
        }

        String userType = (String) session.getAttribute("userType");
        Long userId = (Long) session.getAttribute("userId");
        String userName = (String) session.getAttribute("userName");

        Object details = null;
        if ("CITIZEN".equalsIgnoreCase(userType)) {
            Optional<Citizen> citizenOpt = citizenRepository.findById(userId);
            if (citizenOpt.isPresent()) {
                Citizen c = citizenOpt.get();
                details = new CitizenResponseDto(
                        c.getCitizenId(),
                        c.getFullName(),
                        c.getAadhaarNumber(),
                        c.getEmail(),
                        c.getPhoneNumber(),
                        c.getAddress(),
                        c.getCreatedAt()
                );
            }
        } else if ("OFFICER".equalsIgnoreCase(userType)) {
            Optional<Officer> officerOpt = officerRepository.findById(userId);
            if (officerOpt.isPresent()) {
                Officer o = officerOpt.get();
                details = new OfficerResponseDto(
                        o.getOfficerId(),
                        o.getFullName(),
                        o.getEmployeeId(),
                        o.getDepartment(),
                        o.getEmail(),
                        o.getPhoneNumber(),
                        o.getCreatedAt()
                );
            }
        }

        return SessionInfoDto.authenticated(userType, userId, userName, details);
    }
}
