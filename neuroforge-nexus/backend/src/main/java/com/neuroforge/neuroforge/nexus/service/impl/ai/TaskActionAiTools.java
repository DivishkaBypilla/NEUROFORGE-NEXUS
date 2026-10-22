package com.neuroforge.neuroforge.nexus.service.impl.ai;

import com.neuroforge.neuroforge.nexus.dto.request.CreateTaskRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateTaskStatusRequest;
import com.neuroforge.neuroforge.nexus.dto.response.TaskResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.TaskSecurity;
import com.neuroforge.neuroforge.nexus.service.TaskService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TaskActionAiTools {

    private final TaskService taskService;
    private final TaskSecurity taskSecurity;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskActionAiTools(
            TaskService taskService,
            TaskSecurity taskSecurity,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.taskService = taskService;
        this.taskSecurity = taskSecurity;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }


    // ============================================================
    // FIND PROJECT MEMBERS
    // ============================================================

    @Tool(
            name = "findProjectMembers",
            description = """
                    Find the users who belong to a specific project.

                    Use this when creating a task and the user needs
                    to choose who the task should be assigned to.

                    Only use this after the project has been identified.

                    This tool returns only members of the selected project,
                    not all users in the database.

                    Never invent a user ID.

                    If multiple users have the same or similar names,
                    do not choose randomly. Ask the user to choose
                    using their name and email.
                    """
    )
    public List<ProjectMemberResult> findProjectMembers(String projectId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        /*
         * First check whether the authenticated user
         * can view this project.
         */
        boolean allowed =
                taskSecurity.canViewProjectTasks(
                        projectId,
                        authentication
                );

        if (!allowed) {
            throw new AccessDeniedException(
                    "You do not have access to this project."
            );
        }

        User currentUser = (User) authentication.getPrincipal();

        /*
         * Find the project, scoped to the current user's own organization.
         */
        Project project =
                projectRepository.findByIdAndOrganizationId(projectId, currentUser.getOrganizationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Project not found."
                                )
                        );

        /*
         * If the project has no members,
         * return an empty list.
         */
        if (project.getMemberIds() == null ||
                project.getMemberIds().isEmpty()) {

            return List.of();
        }

        /*
         * Get only users belonging to this project.
         */
        return project.getMemberIds()
                .stream()
                .map(userRepository::findById)
                .flatMap(Optional::stream)
                .map(user -> new ProjectMemberResult(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                ))
                .toList();
    }


    public record ProjectMemberResult(
            String userId,
            String name,
            String email
    ) {
    }


    // ============================================================
    // CREATE TASK
    // ============================================================

    @Tool(
            name = "createTask",
            description = """
                    Create a new task in a project.

                    Use this ONLY when the user explicitly wants
                    to create a task.

                    Required information:
                    - projectId
                    - title
                    - description
                    - assigneeId

                    The projectId must come from the user's accessible
                    projects or information already provided by the user.

                    The assigneeId must come from findProjectMembers()
                    or information explicitly provided by the user.

                    Never invent project IDs or user IDs.

                    If the user provides a person's name instead of
                    an assignee ID, use findProjectMembers() first.

                    If multiple users have the same name, ask the
                    user to clarify using their name and email.

                    The currently authenticated user must have
                    permission to manage tasks in the project.

                    ADMIN, PROJECT_MANAGER, or the project's
                    TEAM LEAD can create tasks.

                    Do not call this tool if required information
                    is missing. Ask the user for the missing
                    information first.

                    The task will initially have TODO status.
                    """
    )
    public TaskCreationResult createTask(
            String projectId,
            String title,
            String description,
            String assigneeId
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User currentUser =
                (User) authentication.getPrincipal();

        /*
         * Check whether the authenticated user
         * can create tasks in this project.
         */
        boolean allowed =
                taskSecurity.canManageProjectTasks(
                        projectId,
                        authentication
                );

        if (!allowed) {
            return new TaskCreationResult(
                    null,
                    null,
                    projectId,
                    null,
                    "You do not have permission to create tasks in this project."
            );
        }

        /*
         * Create the normal application request.
         */
        CreateTaskRequest request =
                new CreateTaskRequest(
                        projectId,
                        title,
                        description,
                        assigneeId,
                        TaskStatus.TODO,
                        null
                );

        /*
         * Let the normal TaskService handle
         * business validation and persistence.
         */
        TaskResponse response =
                taskService.createTask(
                        request,
                        currentUser.getId()
                );

        return new TaskCreationResult(
                response.getId(),
                response.getTitle(),
                response.getProjectId(),
                response.getStatus(),
                "Task created successfully."
        );
    }


    public record TaskCreationResult(
            String taskId,
            String title,
            String projectId,
            TaskStatus status,
            String message
    ) {
    }


    // ============================================================
    // UPDATE TASK STATUS
    // ============================================================

    @Tool(
            name = "updateTaskStatus",
            description = """
                        Update the status of an existing task.
                    
                        Use this when the user explicitly asks to:
                        - change a task status
                        - update a task status
                        - mark a task as done
                        - mark a task as completed
                        - move a task to in progress
                        - move a task back to todo
                    
                        Valid statuses are TODO, IN_PROGRESS, and DONE.
                    
                        The task can be moved from TODO to another status by:
                        - the task's ASSIGNEE
                        - ADMIN
                        - PROJECT_MANAGER
                        - the project's TEAM LEAD
                    
                        A task assignee can update the status of their own
                        assigned task.
                    
                        Never invent a task ID.
                        """
                        )
    public TaskStatusUpdateResult updateTaskStatus(
            String taskId,
            TaskStatus status
    ) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        /*
         * Check whether the current user
         * can update this task's status.
         */
        boolean allowed =
                taskSecurity.canUpdateTaskStatus(
                        taskId,
                        authentication
                );

        if (!allowed) {
            return new TaskStatusUpdateResult(
                    null,
                    taskId,
                    null,
                    "You do not have permission to update this task's status."
            );
        }

        /*
         * Create the normal application request.
         */
        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(status);

        /*
         * Let TaskService handle the actual
         * business logic and persistence.
         */
        TaskResponse response =
                taskService.updateTaskStatus(
                        taskId,
                        request
                );

        return new TaskStatusUpdateResult(
                response.getId(),
                taskId,
                response.getStatus(),
                "Task status updated successfully."
        );
    }


    public record TaskStatusUpdateResult(
            String taskId,
            String requestedTaskId,
            TaskStatus status,
            String message
    ) {
    }
}