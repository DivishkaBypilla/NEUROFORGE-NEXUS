package com.neuroforge.neuroforge.nexus.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GithubCallbackRequest(
        @NotBlank
        String code,

        // Which organization's GitHub OAuth App to complete the exchange
        // against — the same slug the frontend used to fetch /auth/github/config
        // and build the authorize URL in the first place.
        @NotBlank
        String organizationSlug
) {
}
