package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.dto.request.*;
import com.neuroforge.neuroforge.nexus.dto.response.TaskResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.Subtask;
import com.neuroforge.neuroforge.nexus.entities.Task;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import com.neuroforge.neuroforge.nexus.exception.ProjectNotFoundException;
import com.neuroforge.neuroforge.nexus.exception.ResourceNotFoundException;
import com.neuroforge.neuroforge.nexus.mapper.TaskMapper;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.TaskRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.CurrentUserService;
import com.neuroforge.neuroforge.nexus.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final CurrentUserService currentUserService;

    // A task's/subtask's assignee must be an existing user, actually involved in
    // the project (owner, team lead, or a member), and not an ADMIN account —
    // same "admins aren't project team members" rule as ProjectServiceImpl.
    private void validateAssignee(Project project, String assigneeId) {
        User user = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assigneeId));

        if (!java.util.Objects.equals(user.getOrganizationId(), project.getOrganizationId())) {
            throw new ResourceNotFoundException("Assignee not found with id: " + assigneeId);
        }

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot assign a task to an ADMIN account");
        }

        boolean involved = assigneeId.equals(project.getOwnerId())
                || assigneeId.equals(project.getTeamLead())
                || (project.getMemberIds() != null && project.getMemberIds().contains(assigneeId));

        if (!involved) {
            throw new IllegalArgumentException("Assignee must be a member of the project");
        }
    }

    // Organization-scoped — a project id from another organization resolves
    // to "not found" exactly like an unknown id, never leaking its data.
    private Project getProjectOrThrow(String projectId) {
        return projectRepository.findByIdAndOrganizationId(projectId, currentUserService.organizationId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + projectId));
    }

    private Task getTaskOrThrow(String taskId) {
        return taskRepository.findByIdAndOrganizationId(taskId, currentUserService.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private Subtask getSubtaskOrThrow(Task task, String subtaskId) {
        return task.getSubtasks().stream()
                .filter(s -> s.getId().equals(subtaskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with id: " + subtaskId));
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request, String createdByUserId) {
        log.info("Creating task '{}' for project {}", request.getTitle(), request.getProjectId());
        Project project = getProjectOrThrow(request.getProjectId());

        if (request.getAssigneeId() != null) {
            validateAssignee(project, request.getAssigneeId());
        }

        Task task = taskMapper.toEntity(request);
        task.setTaskId(UUID.randomUUID());
        task.setProjectId(project.getId());
        task.setOrganizationId(project.getOrganizationId());
        task.setCreatedBy(createdByUserId);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        if (task.getSubtasks() == null) {
            task.setSubtasks(new ArrayList<>());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task saved = taskRepository.save(task);
        log.info("Task created with ID: {}", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse getTaskById(String id) {
        return taskMapper.toResponse(getTaskOrThrow(id));
    }

    @Override
    public List<TaskResponse> getTasksByProject(String projectId) {
        return taskMapper.toResponseList(
                taskRepository.findByProjectIdAndOrganizationId(projectId, currentUserService.organizationId()));
    }

    @Override
    public List<TaskResponse> getMyTasksInProject(String projectId, String userId) {
        return taskMapper.toResponseList(taskRepository.findByProjectIdAndAssigneeIdAndOrganizationId(
                projectId, userId, currentUserService.organizationId()));
    }

    @Override
    public TaskResponse updateTask(String id, UpdateTaskRequest request) {
        Task task = getTaskOrThrow(id);

        if (request.getAssigneeId() != null) {
            Project project = getProjectOrThrow(task.getProjectId());
            validateAssignee(project, request.getAssigneeId());
        }

        taskMapper.updateEntityFromRequest(request, task);
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse updateTaskStatus(String id, UpdateTaskStatusRequest request) {
        Task task = getTaskOrThrow(id);
        task.setStatus(request.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public void deleteTask(String id) {
        if (!taskRepository.existsByIdAndOrganizationId(id, currentUserService.organizationId())) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponse addSubtask(String taskId, CreateSubtaskRequest request) {
        Task task = getTaskOrThrow(taskId);
        Project project = getProjectOrThrow(task.getProjectId());

        if (request.getAssigneeId() != null) {
            validateAssignee(project, request.getAssigneeId());
        }

        Subtask subtask = Subtask.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .assigneeId(request.getAssigneeId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (task.getSubtasks() == null) {
            task.setSubtasks(new ArrayList<>());
        }
        task.getSubtasks().add(subtask);
        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse updateSubtask(String taskId, String subtaskId, UpdateSubtaskRequest request) {
        Task task = getTaskOrThrow(taskId);
        Subtask subtask = getSubtaskOrThrow(task, subtaskId);

        if (request.getAssigneeId() != null) {
            Project project = getProjectOrThrow(task.getProjectId());
            validateAssignee(project, request.getAssigneeId());
            subtask.setAssigneeId(request.getAssigneeId());
        }
        if (request.getTitle() != null) {
            subtask.setTitle(request.getTitle());
        }
        subtask.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse updateSubtaskStatus(String taskId, String subtaskId, UpdateSubtaskStatusRequest request) {
        Task task = getTaskOrThrow(taskId);
        Subtask subtask = getSubtaskOrThrow(task, subtaskId);

        subtask.setStatus(request.getStatus());
        subtask.setUpdatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse deleteSubtask(String taskId, String subtaskId) {
        Task task = getTaskOrThrow(taskId);
        Subtask subtask = getSubtaskOrThrow(task, subtaskId);
        task.getSubtasks().remove(subtask);
        task.setUpdatedAt(LocalDateTime.now());
        return taskMapper.toResponse(taskRepository.save(task));
    }
}
