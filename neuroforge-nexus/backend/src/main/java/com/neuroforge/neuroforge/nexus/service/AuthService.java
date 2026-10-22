package com.neuroforge.neuroforge.nexus.service;

import com.neuroforge.neuroforge.nexus.dto.request.LoginRequest;
import com.neuroforge.neuroforge.nexus.dto.request.SignupRequest;
import com.neuroforge.neuroforge.nexus.dto.response.LoginResponse;
import com.neuroforge.neuroforge.nexus.dto.response.SignupResponse;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import jakarta.validation.Valid;

public interface AuthService {
    SignupResponse signup(SignupRequest signupRequest);

    LoginResponse login(LoginRequest request);

    SignupResponse createUserWithRole(SignupRequest request, Role role, String organizationId);

    LoginResponse refresh(String refreshToken);
}
