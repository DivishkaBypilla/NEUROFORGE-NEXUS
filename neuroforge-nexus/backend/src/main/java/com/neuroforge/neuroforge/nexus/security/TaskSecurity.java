package com.neuroforge.neuroforge.nexus.security;

import com.neuroforge.neuroforge.nexus.entities.Task;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Backing bean for @PreAuthorize expressions on task endpoints. Task permissions
// don't map cleanly onto the global Role enum: a project's team lead should be
// able to manage that project's tasks even though "team lead" isn't a Role, and
// a developer should only be able to move the status of tasks assigned to them.
//
// Every project/task lookup here is scoped to the current user's own
// organization — a project or task id belonging to a different organization
// is treated exactly like an unknown id, never like "found but forbidden".
//
// Where a lookup misses (unknown, or cross-organization, task/subtask id),
// these methods return true rather than false — we'd rather the
// controller/service answer with a clean 404 than have @PreAuthorize mask a
// missing resource as a 403.
@Component("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurity {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    // Can create/edit/delete tasks (and subtasks) in this project, and set/change
    // any assignee. True for ADMIN, PROJECT_MANAGER, or the project's team lead.
    public boolean canManageProjectTasks(String projectId, Authentication authentication) {
        User user = principal(authentication);
        if (user == null) return false;
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROJECT_MANAGER) return true;

        return projectRepository.findByIdAndOrganizationId(projectId, user.getOrganizationId())
                .map(project -> user.getId().equals(project.getTeamLead()))
                .orElse(false);
    }

    // Same as above, resolved from a task id instead of a project id.
    public boolean canManageTask(String taskId, Authentication authentication) {
        User user = principal(authentication);
        if (user == null) return false;

        Optional<Task> task = taskRepository.findByIdAndOrganizationId(taskId, user.getOrganizationId());
        if (task.isEmpty()) return true;
        return canManageProjectTasks(task.get().getProjectId(), authentication);
    }

    // Anyone who can manage the project's tasks, OR the task's own assignee, may
    // move that task's status.
    public boolean canUpdateTaskStatus(String taskId, Authentication authentication) {
        User user = principal(authentication);
        if (user == null) return false;

        Optional<Task> task = taskRepository.findByIdAndOrganizationId(taskId, user.getOrganizationId());
        if (task.isEmpty()) return true;

        if (user.getId().equals(task.get().getAssigneeId())) return true;
        return canManageProjectTasks(task.get().getProjectId(), authentication);
    }

    // Same idea one level down: the subtask's own assignee, or anyone who can
    // manage the parent project's tasks.
    public boolean canUpdateSubtaskStatus(String taskId, String subtaskId, Authentication authentication) {
        User user = principal(authentication);
        if (user == null) return false;

        Optional<Task> taskOpt = taskRepository.findByIdAndOrganizationId(taskId, user.getOrganizationId());
        if (taskOpt.isEmpty()) return true;

        Task task = taskOpt.get();
        boolean isAssignee = task.getSubtasks().stream()
                .filter(s -> s.getId().equals(subtaskId))
                .findFirst()
                .map(s -> user.getId().equals(s.getAssigneeId()))
                .orElse(true); // unknown subtask id -> let the controller 404

        if (isAssignee) return true;
        return canManageProjectTasks(task.getProjectId(), authentication);
    }

    // Whether this user may view a given project's tasks at all: ADMIN/
    // PROJECT_MANAGER see everything in their own organization, everyone
    // else must be involved (owner, team lead, or member) — mirrors
    // ProjectServiceImpl#getVisibleProjects.
    public boolean canViewProjectTasks(String projectId, Authentication authentication) {
        User user = principal(authentication);
        if (user == null) return false;

        Optional<com.neuroforge.neuroforge.nexus.entities.Project> projectOpt =
                projectRepository.findByIdAndOrganizationId(projectId, user.getOrganizationId());
        if (projectOpt.isEmpty()) return false;

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.PROJECT_MANAGER) return true;

        var project = projectOpt.get();
        return user.getId().equals(project.getOwnerId())
                || user.getId().equals(project.getTeamLead())
                || (project.getMemberIds() != null && project.getMemberIds().contains(user.getId()));
    }

    // Same as above, resolved from a task id.
    public boolean canViewTask(String taskId, Authentication authentication) {
        User user = principal(authentication);
        if (user == null) return false;

        Optional<Task> task = taskRepository.findByIdAndOrganizationId(taskId, user.getOrganizationId());
        if (task.isEmpty()) return true;
        return canViewProjectTasks(task.get().getProjectId(), authentication);
    }

    private User principal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        return (principal instanceof User) ? (User) principal : null;
    }

    // Alias for canViewProjectTasks, used from ProjectController#getProjectById.
    // Kept as a separate, clearly-named method since it's gating a project's own
    // detail page, not its tasks — even though the underlying rule is identical:
    // ADMIN/PROJECT_MANAGER see any project in their org, everyone else only
    // their own (owner, team lead, or member). Without this, a restricted
    // /users/directory wouldn't mean much — anyone could still open any
    // project's page directly.
    public boolean canViewProject(String projectId, Authentication authentication) {
        return canViewProjectTasks(projectId, authentication);
    }
}
