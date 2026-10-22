package com.neuroforge.neuroforge.nexus.service.impl.ai;

import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.Task;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.TaskRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectProgressAiTools {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ProjectProgressAiTools(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Tool(
            name = "getMyProjectProgress",
            description = """
            Get project progress information for projects
            visible to the currently authenticated user.

            Use this tool when the user asks questions such as:
            - How is my project doing?
            - What is the progress of my project?
            - Give me a project status
            - How many tasks are completed?
            - How many tasks are pending?
            - What is the overall progress?
            - Show me my project progress

            Do not invent project or task information.
            Only use the information returned by this tool.
            """
    )
    public List<ProjectProgress> getMyProjectProgress() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return List.of();
        }

        String email = auth.getName();

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            return List.of();
        }

        /*
         * Only get projects that this user is allowed to see, scoped to
         * their own organization.
         */
        List<Project> projects =
                projectRepository.findAllInvolvingUser(user.getOrganizationId(), user.getId());

        return projects.stream()
                .map(this::buildProjectProgress)
                .toList();
    }

    private ProjectProgress buildProjectProgress(Project project) {

        List<Task> tasks =
                taskRepository.findByProjectIdAndOrganizationId(
                        project.getId(),
                        project.getOrganizationId()
                );

        long totalTasks = tasks.size();

        long completedTasks = tasks.stream()
                .filter(task ->
                        task.getStatus().name().equalsIgnoreCase("DONE")
                )
                .count();

        long inProgressTasks = tasks.stream()
                .filter(task ->
                        task.getStatus().name().equalsIgnoreCase("IN_PROGRESS")
                )
                .count();

        long pendingTasks = tasks.stream()
                .filter(task ->
                        task.getStatus().name().equalsIgnoreCase("TODO")
                )
                .count();

        return new ProjectProgress(
                project.getProjectId().toString(),
                project.getName(),
                project.getStatus().name(),
                project.getPriority().name(),
                totalTasks,
                completedTasks,
                inProgressTasks,
                pendingTasks
        );
    }

    public record ProjectProgress(
            String projectId,
            String projectName,
            String projectStatus,
            String priority,
            long totalTasks,
            long completedTasks,
            long inProgressTasks,
            long pendingTasks
    ) {
    }
}