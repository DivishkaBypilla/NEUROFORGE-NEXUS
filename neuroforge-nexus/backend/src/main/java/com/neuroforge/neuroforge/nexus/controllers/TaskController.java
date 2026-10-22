package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.dto.request.*;
import com.neuroforge.neuroforge.nexus.dto.response.TaskResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.service.TaskService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {

    TaskService taskService;

    @PostMapping
    @PreAuthorize("@taskSecurity.canManageProjectTasks(#request.projectId, authentication)")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(request, principal.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@taskSecurity.canViewTask(#id, authentication)")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable String id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Full board for a project (all statuses, all tasks) — everyone involved in
    // the project can see it, even if they can only edit their own cards.
    @GetMapping("/project/{projectId}")
    @PreAuthorize("@taskSecurity.canViewProjectTasks(#projectId, authentication)")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable String projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    // Convenience filter: just this user's tasks within one project.
    @GetMapping("/project/{projectId}/mine")
    @PreAuthorize("@taskSecurity.canViewProjectTasks(#projectId, authentication)")
    public ResponseEntity<List<TaskResponse>> getMyTasksInProject(
            @PathVariable String projectId,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(taskService.getMyTasksInProject(projectId, principal.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@taskSecurity.canManageTask(#id, authentication)")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    // The Kanban "drag a card to another column" endpoint — the task's own
    // assignee can hit this even without full manage rights on the project.
    @PatchMapping("/{id}/status")
    @PreAuthorize("@taskSecurity.canUpdateTaskStatus(#id, authentication)")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@taskSecurity.canManageTask(#id, authentication)")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/subtasks")
    @PreAuthorize("@taskSecurity.canManageTask(#id, authentication)")
    public ResponseEntity<TaskResponse> addSubtask(
            @PathVariable String id,
            @Valid @RequestBody CreateSubtaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addSubtask(id, request));
    }

    @PutMapping("/{id}/subtasks/{subtaskId}")
    @PreAuthorize("@taskSecurity.canManageTask(#id, authentication)")
    public ResponseEntity<TaskResponse> updateSubtask(
            @PathVariable String id,
            @PathVariable String subtaskId,
            @Valid @RequestBody UpdateSubtaskRequest request) {
        return ResponseEntity.ok(taskService.updateSubtask(id, subtaskId, request));
    }

    @PatchMapping("/{id}/subtasks/{subtaskId}/status")
    @PreAuthorize("@taskSecurity.canUpdateSubtaskStatus(#id, #subtaskId, authentication)")
    public ResponseEntity<TaskResponse> updateSubtaskStatus(
            @PathVariable String id,
            @PathVariable String subtaskId,
            @Valid @RequestBody UpdateSubtaskStatusRequest request) {
        return ResponseEntity.ok(taskService.updateSubtaskStatus(id, subtaskId, request));
    }

    @DeleteMapping("/{id}/subtasks/{subtaskId}")
    @PreAuthorize("@taskSecurity.canManageTask(#id, authentication)")
    public ResponseEntity<TaskResponse> deleteSubtask(
            @PathVariable String id,
            @PathVariable String subtaskId) {
        return ResponseEntity.ok(taskService.deleteSubtask(id, subtaskId));
    }
}