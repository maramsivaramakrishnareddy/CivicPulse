package com.smartgovernance.controller;

import com.smartgovernance.dto.*;
import com.smartgovernance.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller exposing endpoints for citizen registration,
 * citizen login, officer login, logout, and service health check.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Citizen Registration Endpoint
     */
    @PostMapping("/citizen/register")
    public ResponseEntity<ApiResponse> registerCitizen(@Valid @RequestBody CitizenRegisterRequest request) {
        CitizenResponseDto response = authService.registerCitizen(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Citizen registration completed successfully", response));
    }

    /**
     * Citizen Login Endpoint
     */
    @PostMapping("/citizen/login")
    public ResponseEntity<ApiResponse> loginCitizen(@Valid @RequestBody CitizenLoginRequest request, HttpSession session) {
        CitizenResponseDto response = authService.loginCitizen(request, session);
        return ResponseEntity.ok(ApiResponse.success("Citizen login successful", response));
    }

    /**
     * Officer Login Endpoint
     */
    @PostMapping("/officer/login")
    public ResponseEntity<ApiResponse> loginOfficer(@Valid @RequestBody OfficerLoginRequest request, HttpSession session) {
        OfficerResponseDto response = authService.loginOfficer(request, session);
        return ResponseEntity.ok(ApiResponse.success("Officer login successful", response));
    }

    /**
     * Logout Endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * Health-check Endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> healthCheck() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "Smart Governance Platform Backend");
        healthData.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(ApiResponse.success("Smart Governance Platform service is running smoothly", healthData));
    }
}
