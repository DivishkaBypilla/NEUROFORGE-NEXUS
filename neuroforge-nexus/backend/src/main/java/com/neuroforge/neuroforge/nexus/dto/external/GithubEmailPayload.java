package com.neuroforge.neuroforge.nexus.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// One entry of the array returned by GET https://api.github.com/user/emails
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubEmailPayload(
        String email,
        boolean primary,
        boolean verified
) {
}
