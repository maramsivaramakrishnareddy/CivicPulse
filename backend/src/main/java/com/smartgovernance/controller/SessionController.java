package com.smartgovernance.controller;

import com.smartgovernance.dto.ApiResponse;
import com.smartgovernance.dto.SessionInfoDto;
import com.smartgovernance.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Session Controller to check active authentication session status.
 */
@RestController
@RequestMapping("/api")
public class SessionController {

    private final AuthService authService;

    public SessionController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Session checking API
     */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse> getSession(HttpSession session) {
        SessionInfoDto sessionInfo = authService.getSessionInfo(session);
        return ResponseEntity.ok(ApiResponse.success("Session status retrieved", sessionInfo));
    }
}
