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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TaskAiTools {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskAiTools(
            TaskRepository taskRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Tool(
            name = "getMyTasks",
            description = """
            Get the tasks assigned to the currently authenticated user.
            Use this when the user asks:
            - What are my tasks?
            - What tasks are assigned to me?
            - Show my tasks
            - What work do I have?
            - What tasks are pending?
            """
    )
    public List<TaskSummary> getMyTasks() {
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

        List<Task> tasks = taskRepository.findByAssigneeIdAndOrganizationId(user.getId(), user.getOrganizationId());

        // Resolve all referenced projects in one batch query instead of one
        // lookup per task, and fall back to the raw ID if a project was
        // since deleted so a stale task still reports something sensible.
        List<String> projectIds = tasks.stream()
                .map(Task::getProjectId)
                .distinct()
                .toList();

        Map<String, String> projectNamesById = projectRepository.findAllById(projectIds)
                .stream()
                .collect(Collectors.toMap(Project::getId, Project::getName));

        return tasks.stream()
                .map(task -> toSummary(task, projectNamesById))
                .toList();
    }

    private TaskSummary toSummary(Task task, Map<String, String> projectNamesById) {

        String projectName = projectNamesById.getOrDefault(task.getProjectId(), task.getProjectId());

        return new TaskSummary(
                task.getTaskId().toString(),
                task.getProjectId(),
                projectName,
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name()
        );
    }

    public record TaskSummary(
            String taskId,
            String projectId,
            String projectName,
            String title,
            String description,
            String status
    ) {}
}