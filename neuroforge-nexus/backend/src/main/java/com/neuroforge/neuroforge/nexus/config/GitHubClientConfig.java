package com.neuroforge.neuroforge.nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class GitHubClientConfig {

    // For POST https://github.com/login/oauth/access_token during the OAuth
    // code exchange. Kept separate from the api.github.com client below
    // because it talks to a different host with a different Accept header.
    @Bean
    public RestClient githubOAuthRestClient() {
        return RestClient.builder()
                .baseUrl("https://github.com")
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // For reading user profile + repo data (issues/PRs/commits) from the
    // GitHub REST API. Per-request Authorization headers (user token or the
    // configured server-side PAT) are added by the calling service.
    @Bean
    public RestClient githubApiRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }
}
