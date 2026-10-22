package com.neuroforge.neuroforge.nexus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
public record GitHubProperties(
        String clientId,
        String clientSecret,
        String redirectUri,
        // Optional server-side personal access token used for reading repo
        // data (issues/PRs/commits). Without it, calls to the GitHub REST API
        // fall back to unauthenticated requests, which only work for public
        // repos and are capped at 60 requests/hour per GitHub's rate limits.
        String apiToken
) {
}
