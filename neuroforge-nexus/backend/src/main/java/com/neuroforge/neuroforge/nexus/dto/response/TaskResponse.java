package com.neuroforge.neuroforge.nexus.dto.response;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskResponse {

    String id;

    UUID taskId;

    String projectId;

    String title;

    String description;

    TaskStatus status;

    String assigneeId;

    LocalDate dueDate;

    String createdBy;

    List<SubtaskResponse> subtasks;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}