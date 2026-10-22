package com.neuroforge.neuroforge.nexus.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// One entry from GET /repos/{owner}/{repo}/commits
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubCommitPayload(
        String sha,
        GithubCommitDetail commit,
        GithubUserPayload author,
        @JsonProperty("html_url") String htmlUrl
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GithubCommitDetail(
            String message,
            GithubCommitAuthor author
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GithubCommitAuthor(
            String name,
            String date
    ) {
    }
}
