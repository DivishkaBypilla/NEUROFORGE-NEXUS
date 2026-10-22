package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.dto.external.GithubCommitPayload;
import com.neuroforge.neuroforge.nexus.dto.external.GithubIssuePayload;
import com.neuroforge.neuroforge.nexus.dto.response.GithubCommitResponse;
import com.neuroforge.neuroforge.nexus.dto.response.GithubIssueResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.Task;
import com.neuroforge.neuroforge.nexus.exception.GithubIntegrationException;
import com.neuroforge.neuroforge.nexus.exception.ProjectNotFoundException;
import com.neuroforge.neuroforge.nexus.exception.ResourceNotFoundException;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.TaskRepository;
import com.neuroforge.neuroforge.nexus.security.CurrentUserService;
import com.neuroforge.neuroforge.nexus.service.GitHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubServiceImpl implements GitHubService {

    private final RestClient githubApiRestClient;
    private final GitHubSettingsService gitHubSettingsService;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final CurrentUserService currentUserService;

    @Override
    public List<GithubIssueResponse> getOpenPullRequests(String projectId) {
        RepoRef repo = repoFor(projectId);
        return fetchIssuesOrPulls(repo, "pulls", "open", 30).stream()
                .map(this::toIssueResponse)
                .toList();
    }

    @Override
    public List<GithubIssueResponse> getOpenIssues(String projectId) {
        RepoRef repo = repoFor(projectId);
        return fetchIssuesOrPulls(repo, "issues", "open", 30).stream()
                // GitHub's /issues endpoint also returns pull requests; exclude
                // those here since getOpenPullRequests already covers them.
                .filter(item -> !item.isPullRequest())
                .map(this::toIssueResponse)
                .toList();
    }

    @Override
    public List<GithubCommitResponse> getRecentCommits(String projectId) {
        RepoRef repo = repoFor(projectId);

        List<GithubCommitPayload> commits;
        try {
            commits = githubApiRestClient.get()
                    .uri("/repos/{owner}/{repo}/commits?per_page=20", repo.owner(), repo.repo())
                    .headers(this::applyAuth)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException ex) {
            throw githubFetchFailed(repo, ex);
        }

        if (commits == null) return List.of();

        return commits.stream().map(this::toCommitResponse).toList();
    }

    @Override
    public List<GithubIssueResponse> getLinkedItemsForTask(String taskId) {
        String organizationId = currentUserService.organizationId();

        Task task = taskRepository.findByIdAndOrganizationId(taskId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Project project = projectRepository.findByIdAndOrganizationId(task.getProjectId(), organizationId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + task.getProjectId()));

        if (project.getGithubRepo() == null || project.getGithubRepo().isBlank()) {
            return List.of();
        }

        RepoRef repo = parseRepo(project.getGithubRepo());
        String taskRef = taskRef(task);

        // Search both open and closed items — a task is often still worth
        // showing its linked PR after that PR has been merged/closed.
        List<GithubIssuePayload> pulls = fetchIssuesOrPulls(repo, "pulls", "all", 50);
        List<GithubIssuePayload> issues = fetchIssuesOrPulls(repo, "issues", "all", 50).stream()
                .filter(item -> !item.isPullRequest())
                .toList();

        return Stream.concat(pulls.stream(), issues.stream())
                .filter(item -> references(item, taskRef, task))
                .map(this::toIssueResponse)
                .toList();
    }

    // The lightweight "smart commit" style convention this integration relies
    // on, in lieu of a webhook-driven automatic linker: reference a task in a
    // PR/issue title, description, or branch name as e.g. "TASK-a1b2c3d4".
    private String taskRef(Task task) {
        return "TASK-" + task.getTaskId().toString().substring(0, 8);
    }

    private boolean references(GithubIssuePayload item, String taskRef, Task task) {
        String ref = taskRef.toLowerCase(Locale.ROOT);
        String fullId = task.getTaskId().toString().toLowerCase(Locale.ROOT);

        String title = item.title() != null ? item.title().toLowerCase(Locale.ROOT) : "";
        String body = item.body() != null ? item.body().toLowerCase(Locale.ROOT) : "";
        String branch = item.head() != null && item.head().ref() != null
                ? item.head().ref().toLowerCase(Locale.ROOT)
                : "";

        return title.contains(ref) || body.contains(ref) || branch.contains(ref)
                || title.contains(fullId) || body.contains(fullId) || branch.contains(fullId);
    }

    private List<GithubIssuePayload> fetchIssuesOrPulls(RepoRef repo, String resource, String state, int perPage) {
        try {
            List<GithubIssuePayload> items = githubApiRestClient.get()
                    .uri("/repos/{owner}/{repo}/" + resource + "?state={state}&per_page={perPage}",
                            repo.owner(), repo.repo(), state, perPage)
                    .headers(this::applyAuth)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return items != null ? items : List.of();
        } catch (RestClientException ex) {
            throw githubFetchFailed(repo, ex);
        }
    }

    private void applyAuth(HttpHeaders headers) {
        String apiToken = gitHubSettingsService.resolve(currentUserService.organizationId()).apiToken();
        if (apiToken != null && !apiToken.isBlank()) {
            headers.setBearerAuth(apiToken);
        }
        // Without a configured token, requests go out unauthenticated — fine
        // for public repos, but capped at GitHub's 60 requests/hour limit.
    }

    private GithubIntegrationException githubFetchFailed(RepoRef repo, RestClientException ex) {
        log.error("GitHub API call failed for repo {}/{}", repo.owner(), repo.repo(), ex);
        return new GithubIntegrationException(
                "Could not fetch data from GitHub for " + repo.owner() + "/" + repo.repo() +
                ". Check the repo name and (for private repos) that an API token is set under Settings.");
    }

    private RepoRef repoFor(String projectId) {
        Project project = projectRepository.findByIdAndOrganizationId(projectId, currentUserService.organizationId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
        return parseRepo(project.getGithubRepo());
    }

    private RepoRef parseRepo(String githubRepo) {
        if (githubRepo == null || githubRepo.isBlank()) {
            throw new GithubIntegrationException("This project has no GitHub repository linked yet.");
        }
        String[] parts = githubRepo.split("/");
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new GithubIntegrationException(
                    "Invalid GitHub repo format \"" + githubRepo + "\" — expected \"owner/repo\".");
        }
        return new RepoRef(parts[0], parts[1]);
    }

    private GithubIssueResponse toIssueResponse(GithubIssuePayload item) {
        return GithubIssueResponse.builder()
                .number(item.number())
                .title(item.title())
                .state(item.state())
                .url(item.htmlUrl())
                .authorLogin(item.user() != null ? item.user().login() : null)
                .branchRef(item.head() != null ? item.head().ref() : null)
                .createdAt(item.createdAt())
                .updatedAt(item.updatedAt())
                .build();
    }

    private GithubCommitResponse toCommitResponse(GithubCommitPayload item) {
        return GithubCommitResponse.builder()
                .sha(item.sha() != null ? item.sha().substring(0, Math.min(7, item.sha().length())) : null)
                .message(item.commit() != null && item.commit().message() != null
                        ? item.commit().message().lines().findFirst().orElse("")
                        : "")
                .authorName(item.commit() != null && item.commit().author() != null ? item.commit().author().name() : null)
                .authorLogin(item.author() != null ? item.author().login() : null)
                .date(item.commit() != null && item.commit().author() != null ? item.commit().author().date() : null)
                .url(item.htmlUrl())
                .build();
    }

    private record RepoRef(String owner, String repo) {}
}
