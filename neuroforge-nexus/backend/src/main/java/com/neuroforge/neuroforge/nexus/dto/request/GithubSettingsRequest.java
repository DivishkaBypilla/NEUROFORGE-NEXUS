package com.neuroforge.neuroforge.nexus.dto.request;

// Blank/omitted clientSecret or apiToken means "keep what's already saved" —
// the settings form never has to re-send a secret it can't see to redisplay.
public record GithubSettingsRequest(
        String clientId,
        String clientSecret,
        String apiToken,
        String redirectUri
) {
}
