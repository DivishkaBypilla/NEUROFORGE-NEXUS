package com.neuroforge.neuroforge.nexus.service;

import com.neuroforge.neuroforge.nexus.dto.response.GithubCommitResponse;
import com.neuroforge.neuroforge.nexus.dto.response.GithubIssueResponse;

import java.util.List;

public interface GitHubService {

    List<GithubIssueResponse> getOpenPullRequests(String projectId);

    List<GithubIssueResponse> getOpenIssues(String projectId);

    List<GithubCommitResponse> getRecentCommits(String projectId);

    // PRs and issues (open or closed) whose title, body, or branch name
    // reference this task — see GitHubServiceImpl for the "TASK-xxxxxxxx"
    // convention this matches on.
    List<GithubIssueResponse> getLinkedItemsForTask(String taskId);
}
