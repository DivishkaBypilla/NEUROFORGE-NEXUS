package com.neuroforge.neuroforge.nexus.service.impl.ai;

import com.neuroforge.neuroforge.nexus.dto.response.ProjectResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.service.ProjectService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectAiTools {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    public ProjectAiTools(ProjectService projectService, UserRepository userRepository) {
        this.projectService = projectService;
        this.userRepository = userRepository;
    }

    @Tool(
            name = "getMyProjects",
            description = """
            Get the projects that the currently authenticated user
            is allowed to see.

            Use this tool when the user asks:
            - What projects am I working on?
            - Show my projects
            - What projects am I involved in?
            - Which projects do I belong to?
            - Tell me about my projects
            """
    )
    public List<ProjectSummary> getMyProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return List.of();
        }

        User currentUser = null;

        // Try extracting principal directly if it's already a User entity
        if (auth.getPrincipal() instanceof User userPrincipal) {
            currentUser = userPrincipal;
        } else {
            // Fallback: fetch User entity by email/name from the authentication
            String email = auth.getName();
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        if (currentUser == null) {
            return List.of();
        }

        List<ProjectResponse> projects = projectService.getVisibleProjects(currentUser);

        return projects.stream()
                .map(this::toSummary)
                .toList();
    }

    private ProjectSummary toSummary(ProjectResponse project) {
        return new ProjectSummary(
                project.getProjectId(),
                project.getName(),
                project.getDescription(),
                project.getStatus() != null ? project.getStatus().name() : null,
                project.getPriority() != null ? project.getPriority().name() : null,
                project.getOwnerId(),
                project.getTeamLead(),
                project.getMemberIds()
        );
    }

    public record ProjectSummary(
            java.util.UUID projectId,
            String name,
            String description,
            String status,
            String priority,
            String ownerId,
            String teamLead,
            List<String> memberIds
    ) {
    }
}