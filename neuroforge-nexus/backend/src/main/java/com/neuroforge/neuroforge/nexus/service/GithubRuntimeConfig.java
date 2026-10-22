package com.neuroforge.neuroforge.nexus.service;

public record GithubRuntimeConfig(
        String clientId,
        String clientSecret,
        String apiToken,
        String redirectUri
) {
    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
