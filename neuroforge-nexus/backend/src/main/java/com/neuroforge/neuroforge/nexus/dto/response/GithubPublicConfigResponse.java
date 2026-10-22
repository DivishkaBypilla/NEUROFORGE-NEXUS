package com.neuroforge.neuroforge.nexus.dto.response;

public record GithubPublicConfigResponse(
        boolean enabled,
        String clientId,
        String redirectUri
) {
}
