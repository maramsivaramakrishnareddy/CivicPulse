package com.smartgovernance.service;

import com.smartgovernance.dto.*;
import jakarta.servlet.http.HttpSession;

/**
 * Service interface for authentication and citizen/officer session management.
 */
public interface AuthService {

    CitizenResponseDto registerCitizen(CitizenRegisterRequest request);

    CitizenResponseDto loginCitizen(CitizenLoginRequest request, HttpSession session);

    OfficerResponseDto loginOfficer(OfficerLoginRequest request, HttpSession session);

    void logout(HttpSession session);

    SessionInfoDto getSessionInfo(HttpSession session);
}
