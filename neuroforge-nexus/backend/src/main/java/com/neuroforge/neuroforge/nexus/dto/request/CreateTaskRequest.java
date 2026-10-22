package com.neuroforge.neuroforge.nexus.dto.request;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskRequest {

    @NotBlank(message = "Project ID is required")
    String projectId;

    @NotBlank(message = "Task title is required")
    String title;

    String description;

    String assigneeId;

    TaskStatus status;

    LocalDate dueDate;
}