package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.dto.request.GithubSettingsRequest;
import com.neuroforge.neuroforge.nexus.dto.response.GithubSettingsResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.service.impl.GitHubSettingsService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Read/write access here is gated the same way the rest of the app gates
// admin-style actions: the USER_MANAGE authority, held by ADMIN and
// PROJECT_MANAGER (see RolePermission) — so a project manager or admin can
// set this up from the running app, no redeploy or env var needed. Every
// operation is scoped to the caller's own organization (principal.getOrganizationId())
// — there is no way to view or change another organization's GitHub App
// credentials, even for an ADMIN.
@RestController
@RequestMapping("/settings/github")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubSettingsController {

    GitHubSettingsService gitHubSettingsService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<GithubSettingsResponse> get(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(gitHubSettingsService.getMasked(principal.getOrganizationId()));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<GithubSettingsResponse> update(
            @Valid @RequestBody GithubSettingsRequest request,
            @AuthenticationPrincipal User principal
    ) {
        return ResponseEntity.ok(
                gitHubSettingsService.save(request, principal.getOrganizationId(), principal.getEmail()));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> clear(@AuthenticationPrincipal User principal) {
        gitHubSettingsService.clear(principal.getOrganizationId());
        return ResponseEntity.noContent().build();
    }
}
