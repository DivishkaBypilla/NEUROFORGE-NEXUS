package com.neuroforge.neuroforge.nexus.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Shared shape for one entry from both:
//   GET /repos/{owner}/{repo}/pulls
//   GET /repos/{owner}/{repo}/issues
// GitHub's "issues" endpoint also returns pull requests (each item then has a
// non-null "pull_request" field) — pullRequest here is only used to filter
// those out when we specifically want plain issues.
@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubIssuePayload(
        Long id,
        Integer number,
        String title,
        String body,
        String state,
        @JsonProperty("html_url") String htmlUrl,
        GithubUserPayload user,
        @JsonProperty("created_at") String createdAt,
        @JsonProperty("updated_at") String updatedAt,
        @JsonProperty("pull_request") Object pullRequest,
        GithubBranchRef head
) {
    public boolean isPullRequest() {
        return pullRequest != null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GithubBranchRef(String ref) {
    }
}
