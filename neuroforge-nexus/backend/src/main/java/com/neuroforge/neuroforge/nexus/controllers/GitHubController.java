package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.dto.response.GithubCommitResponse;
import com.neuroforge.neuroforge.nexus.dto.response.GithubIssueResponse;
import com.neuroforge.neuroforge.nexus.service.GitHubService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GitHubController {

    GitHubService gitHubService;

    @GetMapping("/projects/{id}/github/pulls")
    @PreAuthorize("@taskSecurity.canViewProject(#id, authentication)")
    public ResponseEntity<List<GithubIssueResponse>> getOpenPullRequests(@PathVariable String id) {
        return ResponseEntity.ok(gitHubService.getOpenPullRequests(id));
    }

    @GetMapping("/projects/{id}/github/issues")
    @PreAuthorize("@taskSecurity.canViewProject(#id, authentication)")
    public ResponseEntity<List<GithubIssueResponse>> getOpenIssues(@PathVariable String id) {
        return ResponseEntity.ok(gitHubService.getOpenIssues(id));
    }

    @GetMapping("/projects/{id}/github/commits")
    @PreAuthorize("@taskSecurity.canViewProject(#id, authentication)")
    public ResponseEntity<List<GithubCommitResponse>> getRecentCommits(@PathVariable String id) {
        return ResponseEntity.ok(gitHubService.getRecentCommits(id));
    }

    @GetMapping("/tasks/{id}/github")
    @PreAuthorize("@taskSecurity.canViewTask(#id, authentication)")
    public ResponseEntity<List<GithubIssueResponse>> getLinkedItemsForTask(@PathVariable String id) {
        return ResponseEntity.ok(gitHubService.getLinkedItemsForTask(id));
    }
}
