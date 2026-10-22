package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.dto.request.GithubCallbackRequest;
import com.neuroforge.neuroforge.nexus.dto.response.GithubPublicConfigResponse;
import com.neuroforge.neuroforge.nexus.dto.response.LoginResponse;
import com.neuroforge.neuroforge.nexus.entities.Organization;
import com.neuroforge.neuroforge.nexus.repository.OrganizationRepository;
import com.neuroforge.neuroforge.nexus.service.GitHubAuthService;
import com.neuroforge.neuroforge.nexus.service.impl.GitHubSettingsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Mounted under /auth, same as AuthController, so it inherits the existing
// "/auth/**" permitAll rule in WebSecurityConfig — no security config changes
// needed for this to be reachable pre-login.
@RestController
@RequestMapping("/auth/github")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubAuthController {

    GitHubAuthService gitHubAuthService;
    GitHubSettingsService gitHubSettingsService;
    OrganizationRepository organizationRepository;

    // Public, secret-free config the Login/Signup pages read at runtime, for
    // the organization the person is signing into, to decide whether to show
    // "Continue with GitHub" and to build that org's OAuth authorize URL —
    // no frontend env var or rebuild needed when an org's admin changes
    // their GitHub App's client ID under Settings.
    @GetMapping("/config")
    public ResponseEntity<GithubPublicConfigResponse> config(@RequestParam(required = false) String org) {
        if (org == null || org.isBlank()) {
            return ResponseEntity.ok(new GithubPublicConfigResponse(false, null, null));
        }

        return organizationRepository.findBySlug(org.trim().toLowerCase())
                .map(organization -> ResponseEntity.ok(gitHubSettingsService.publicConfig(organization.getId())))
                .orElseGet(() -> ResponseEntity.ok(new GithubPublicConfigResponse(false, null, null)));
    }

    @PostMapping("/callback")
    public ResponseEntity<LoginResponse> callback(
            @Valid @RequestBody GithubCallbackRequest request,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = gitHubAuthService.loginWithCode(request.code(), request.organizationSlug());

        // Same refresh-token cookie convention as the normal /auth/login endpoint.
        Cookie cookie = new Cookie("refreshToken", loginResponse.refreshToken());
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(loginResponse);
    }
}
