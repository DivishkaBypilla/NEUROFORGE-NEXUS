package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.entities.JenkinsSettings;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.repository.JenkinsSettingsRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings/jenkins")
@RequiredArgsConstructor
public class JenkinsController {
    private final JenkinsSettingsRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public Map<String,Object> get(@AuthenticationPrincipal User user) {
        JenkinsSettings s = repository.findById(user.getOrganizationId()).orElse(null);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("configured", s != null && s.getBaseUrl() != null && s.getUsername() != null && s.getApiToken() != null);
        out.put("baseUrl", s == null ? "" : safe(s.getBaseUrl()));
        out.put("username", s == null ? "" : safe(s.getUsername()));
        out.put("defaultJob", s == null ? "" : safe(s.getDefaultJob()));
        out.put("hasApiToken", s != null && s.getApiToken() != null && !s.getApiToken().isBlank());
        out.put("updatedAt", s == null ? null : s.getUpdatedAt());
        out.put("updatedBy", s == null ? null : s.getUpdatedBy());
        return out;
    }

    @PutMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public Map<String,Object> save(@RequestBody SaveRequest req, @AuthenticationPrincipal User user) {
        if (req.baseUrl() == null || req.baseUrl().isBlank() || req.username() == null || req.username().isBlank())
            throw new IllegalArgumentException("Jenkins URL and username are required.");
        String url = req.baseUrl().trim().replaceAll("/+$", "");
        if (!url.startsWith("https://") && !url.startsWith("http://")) throw new IllegalArgumentException("Jenkins URL must start with http:// or https://.");
        JenkinsSettings s = repository.findById(user.getOrganizationId()).orElseGet(() -> JenkinsSettings.builder().id(user.getOrganizationId()).build());
        s.setBaseUrl(url); s.setUsername(req.username().trim());
        if (req.apiToken() != null && !req.apiToken().isBlank()) s.setApiToken(req.apiToken().trim());
        s.setDefaultJob(req.defaultJob() == null ? "" : req.defaultJob().trim());
        s.setUpdatedAt(LocalDateTime.now()); s.setUpdatedBy(user.getEmail()); repository.save(s);
        return get(user);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user) {
        repository.deleteById(user.getOrganizationId()); return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public Map<String,Object> test(@AuthenticationPrincipal User user) {
        JenkinsSettings s = repository.findById(user.getOrganizationId()).orElseThrow(() -> new IllegalArgumentException("Save Jenkins settings first."));
        if (s.getApiToken() == null || s.getApiToken().isBlank()) throw new IllegalArgumentException("Jenkins API token is missing.");
        try {
            String body = RestClient.builder().baseUrl(s.getBaseUrl()).defaultHeaders(h -> h.setBasicAuth(s.getUsername(), s.getApiToken())).build()
                    .get().uri("/api/json?tree=mode,nodeDescription").retrieve().body(String.class);
            return Map.of("connected", true, "message", "Jenkins connection successful.", "details", body == null ? "" : body);
        } catch (RestClientResponseException ex) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_GATEWAY, "Jenkins returned HTTP " + ex.getStatusCode().value() + ". Check URL, username, token, and network access.");
        } catch (Exception ex) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not connect to Jenkins. Check the URL and network access.");
        }
    }

    private String safe(String value) { return value == null ? "" : value; }
    public record SaveRequest(@NotBlank String baseUrl, @NotBlank String username, String apiToken, String defaultJob) {}
}
