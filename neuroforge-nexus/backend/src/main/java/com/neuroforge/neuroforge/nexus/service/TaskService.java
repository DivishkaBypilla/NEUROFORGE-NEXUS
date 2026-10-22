package com.neuroforge.neuroforge.nexus.service;

import com.neuroforge.neuroforge.nexus.dto.request.*;
import com.neuroforge.neuroforge.nexus.dto.response.TaskResponse;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request, String createdByUserId);

    TaskResponse getTaskById(String id);

    List<TaskResponse> getTasksByProject(String projectId);

    List<TaskResponse> getMyTasksInProject(String projectId, String userId);

    TaskResponse updateTask(String id, UpdateTaskRequest request);

    TaskResponse updateTaskStatus(String id, UpdateTaskStatusRequest request);

    void deleteTask(String id);

    TaskResponse addSubtask(String taskId, CreateSubtaskRequest request);

    TaskResponse updateSubtask(String taskId, String subtaskId, UpdateSubtaskRequest request);

    TaskResponse updateSubtaskStatus(String taskId, String subtaskId, UpdateSubtaskStatusRequest request);

    TaskResponse deleteSubtask(String taskId, String subtaskId);
}