package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.dto.external.GithubEmailPayload;
import com.neuroforge.neuroforge.nexus.dto.external.GithubTokenResponse;
import com.neuroforge.neuroforge.nexus.dto.external.GithubUserPayload;
import com.neuroforge.neuroforge.nexus.dto.response.LoginResponse;
import com.neuroforge.neuroforge.nexus.entities.Organization;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.exception.GithubIntegrationException;
import com.neuroforge.neuroforge.nexus.exception.ResourceNotFoundException;
import com.neuroforge.neuroforge.nexus.repository.OrganizationRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.JwtUtil;
import com.neuroforge.neuroforge.nexus.service.GitHubAuthService;
import com.neuroforge.neuroforge.nexus.service.GithubRuntimeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubAuthServiceImpl implements GitHubAuthService {

    private final RestClient githubOAuthRestClient;
    private final RestClient githubApiRestClient;
    private final GitHubSettingsService gitHubSettingsService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse loginWithCode(String code, String organizationSlug) {
        Organization organization = organizationRepository.findBySlug(organizationSlug.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No organization found with slug: " + organizationSlug));

        GithubRuntimeConfig config = gitHubSettingsService.resolve(organization.getId());
        if (!config.isConfigured()) {
            throw new GithubIntegrationException(
                    "GitHub sign-in isn't configured for this organization yet. An admin or project manager needs to set it up under Settings.");
        }

        String githubAccessToken = exchangeCodeForToken(code, config);
        GithubUserPayload githubUser = fetchGithubUser(githubAccessToken);
        String email = resolveEmail(githubUser, githubAccessToken);

        User user = upsertUser(githubUser, email, organization);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        log.info("GitHub login success for {} in organization {}", user.getEmail(), organization.getSlug());
        return new LoginResponse(user.getUserId(), accessToken, refreshToken);
    }

    private String exchangeCodeForToken(String code, GithubRuntimeConfig config) {
        GithubTokenResponse tokenResponse;
        try {
            tokenResponse = githubOAuthRestClient.post()
                    .uri("/login/oauth/access_token")
                    .body(Map.of(
                            "client_id", config.clientId(),
                            "client_secret", config.clientSecret(),
                            "code", code,
                            "redirect_uri", config.redirectUri()
                    ))
                    .retrieve()
                    .body(GithubTokenResponse.class);
        } catch (RestClientException ex) {
            log.error("GitHub token exchange failed", ex);
            throw new GithubIntegrationException("Could not reach GitHub to complete sign-in.");
        }

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            String reason = tokenResponse != null && tokenResponse.errorDescription() != null
                    ? tokenResponse.errorDescription()
                    : "GitHub did not issue an access token for that code.";
            throw new GithubIntegrationException("GitHub sign-in failed: " + reason);
        }

        return tokenResponse.accessToken();
    }

    private GithubUserPayload fetchGithubUser(String githubAccessToken) {
        try {
            GithubUserPayload payload = githubApiRestClient.get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .retrieve()
                    .body(GithubUserPayload.class);

            if (payload == null || payload.id() == null) {
                throw new GithubIntegrationException("GitHub did not return a valid user profile.");
            }
            return payload;
        } catch (RestClientException ex) {
            log.error("Fetching GitHub user profile failed", ex);
            throw new GithubIntegrationException("Could not fetch your GitHub profile.");
        }
    }

    // GitHub's /user response omits email when it's kept private in account
    // settings, so fall back to the verified primary address from /user/emails.
    private String resolveEmail(GithubUserPayload githubUser, String githubAccessToken) {
        if (githubUser.email() != null && !githubUser.email().isBlank()) {
            return githubUser.email();
        }

        try {
            List<GithubEmailPayload> emails = githubApiRestClient.get()
                    .uri("/user/emails")
                    .header("Authorization", "Bearer " + githubAccessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (emails == null) return null;

            return emails.stream()
                    .filter(GithubEmailPayload::primary)
                    .filter(GithubEmailPayload::verified)
                    .map(GithubEmailPayload::email)
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .filter(GithubEmailPayload::verified)
                            .map(GithubEmailPayload::email)
                            .findFirst()
                            .orElse(null));
        } catch (RestClientException ex) {
            log.warn("Could not fetch GitHub email list for user {}", githubUser.login(), ex);
            return null;
        }
    }

    private User upsertUser(GithubUserPayload githubUser, String email, Organization organization) {
        String githubId = String.valueOf(githubUser.id());

        Optional<User> byGithubId = userRepository.findByGithubId(githubId);
        if (byGithubId.isPresent()) {
            User user = byGithubId.get();
            // This exact GitHub identity already belongs to a NeuroForge
            // account in a DIFFERENT organization — refuse rather than
            // silently logging them into the wrong company's workspace.
            if (!organization.getId().equals(user.getOrganizationId())) {
                throw new GithubIntegrationException(
                        "This GitHub account is already linked to a different organization in NeuroForge Nexus.");
            }
            user.setGithubUsername(githubUser.login());
            user.setAvatarUrl(githubUser.avatarUrl());
            return userRepository.save(user);
        }

        if (email != null) {
            Optional<User> byEmail = userRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                User user = byEmail.get();
                // Same guard as above, keyed by email instead of GitHub id —
                // e.g. someone already has a NeuroForge account with this
                // email in another organization.
                if (!organization.getId().equals(user.getOrganizationId())) {
                    throw new GithubIntegrationException(
                            "An account with this email already exists in a different organization in NeuroForge Nexus.");
                }
                // Link an existing email/password account to this GitHub
                // identity instead of creating a duplicate user.
                user.setGithubId(githubId);
                user.setGithubUsername(githubUser.login());
                user.setAvatarUrl(githubUser.avatarUrl());
                log.info("Linked existing account {} to GitHub id {}", email, githubId);
                return userRepository.save(user);
            }
        }

        if (email == null) {
            throw new GithubIntegrationException(
                    "GitHub didn't share a verified email for this account. Add/verify an email on GitHub, " +
                    "or sign up here with a password first and it'll link automatically next time.");
        }

        User newUser = User.builder()
                .userId(UUID.randomUUID())
                .name(githubUser.name() != null && !githubUser.name().isBlank() ? githubUser.name() : githubUser.login())
                .email(email)
                .role(Role.DEVELOPER)
                .organizationId(organization.getId())
                .password(null) // GitHub-only account — no local password set
                .githubId(githubId)
                .githubUsername(githubUser.login())
                .avatarUrl(githubUser.avatarUrl())
                .build();

        log.info("Creating new account via GitHub sign-in: {} in organization {}", email, organization.getSlug());
        return userRepository.save(newUser);
    }
}
