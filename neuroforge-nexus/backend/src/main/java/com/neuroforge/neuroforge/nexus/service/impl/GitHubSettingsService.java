package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.config.GitHubProperties;
import com.neuroforge.neuroforge.nexus.dto.request.GithubSettingsRequest;
import com.neuroforge.neuroforge.nexus.dto.response.GithubPublicConfigResponse;
import com.neuroforge.neuroforge.nexus.dto.response.GithubSettingsResponse;
import com.neuroforge.neuroforge.nexus.entities.IntegrationSettings;
import com.neuroforge.neuroforge.nexus.repository.IntegrationSettingsRepository;
import com.neuroforge.neuroforge.nexus.service.GithubRuntimeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// One GitHub OAuth App configuration per organization (tenant) — each
// document is keyed by organizationId, so Company A and Company B each have
// their own Client ID/Secret/API token and never share GitHub access.
@Service
@RequiredArgsConstructor
public class GitHubSettingsService {

    private final IntegrationSettingsRepository repository;
    private final GitHubProperties fallbackProperties;

    // Effective config for one organization: whatever that org's admin has
    // saved in Settings, falling back to the GITHUB_CLIENT_ID/etc. env vars
    // only if that organization has never configured anything (useful as a
    // first-boot default on a single-tenant deployment).
    public GithubRuntimeConfig resolve(String organizationId) {
        return repository.findById(organizationId)
                .map(s -> new GithubRuntimeConfig(
                        blankToNull(s.getClientId()),
                        blankToNull(s.getClientSecret()),
                        blankToNull(s.getApiToken()),
                        s.getRedirectUri() != null && !s.getRedirectUri().isBlank()
                                ? s.getRedirectUri()
                                : fallbackProperties.redirectUri()
                ))
                .orElseGet(() -> new GithubRuntimeConfig(
                        fallbackProperties.clientId(),
                        fallbackProperties.clientSecret(),
                        fallbackProperties.apiToken(),
                        fallbackProperties.redirectUri()
                ));
    }

    public GithubPublicConfigResponse publicConfig(String organizationId) {
        GithubRuntimeConfig config = resolve(organizationId);
        return new GithubPublicConfigResponse(
                config.isConfigured(),
                config.isConfigured() ? config.clientId() : null,
                config.redirectUri()
        );
    }

    public GithubSettingsResponse getMasked(String organizationId) {
        return repository.findById(organizationId)
                .map(this::toMasked)
                .orElseGet(() -> GithubSettingsResponse.builder()
                        .clientId(fallbackProperties.clientId())
                        .hasClientSecret(fallbackProperties.clientSecret() != null && !fallbackProperties.clientSecret().isBlank())
                        .hasApiToken(fallbackProperties.apiToken() != null && !fallbackProperties.apiToken().isBlank())
                        .redirectUri(fallbackProperties.redirectUri())
                        .configured(resolve(organizationId).isConfigured())
                        .build());
    }

    // Blank clientSecret/apiToken/redirectUri means "leave the current value
    // alone" — the settings form can't (and shouldn't) redisplay a saved
    // secret, so resaving other fields must not wipe it.
    public GithubSettingsResponse save(GithubSettingsRequest request, String organizationId, String updatedByEmail) {
        IntegrationSettings existing = repository.findById(organizationId).orElse(null);

        IntegrationSettings updated = IntegrationSettings.builder()
                .id(organizationId)
                .clientId(nonBlankOr(request.clientId(), existing != null ? existing.getClientId() : null))
                .clientSecret(nonBlankOr(request.clientSecret(), existing != null ? existing.getClientSecret() : null))
                .apiToken(nonBlankOr(request.apiToken(), existing != null ? existing.getApiToken() : null))
                .redirectUri(nonBlankOr(request.redirectUri(), existing != null ? existing.getRedirectUri() : null))
                .updatedAt(LocalDateTime.now())
                .updatedBy(updatedByEmail)
                .build();

        return toMasked(repository.save(updated));
    }

    public void clear(String organizationId) {
        repository.deleteById(organizationId);
    }

    private GithubSettingsResponse toMasked(IntegrationSettings s) {
        boolean hasClientId = s.getClientId() != null && !s.getClientId().isBlank();
        boolean hasSecret = s.getClientSecret() != null && !s.getClientSecret().isBlank();
        return GithubSettingsResponse.builder()
                .clientId(s.getClientId())
                .hasClientSecret(hasSecret)
                .hasApiToken(s.getApiToken() != null && !s.getApiToken().isBlank())
                .redirectUri(s.getRedirectUri())
                .configured(hasClientId && hasSecret)
                .updatedAt(s.getUpdatedAt())
                .updatedBy(s.getUpdatedBy())
                .build();
    }

    private static String nonBlankOr(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    private static String blankToNull(String value) {
        return value != null && !value.isBlank() ? value : null;
    }
}
