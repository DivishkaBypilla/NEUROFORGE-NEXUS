package com.neuroforge.neuroforge.nexus.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Response body of GET https://api.github.com/user
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubUserPayload(
        Long id,
        String login,
        String name,
        String email,
        @JsonProperty("avatar_url") String avatarUrl
) {
}
