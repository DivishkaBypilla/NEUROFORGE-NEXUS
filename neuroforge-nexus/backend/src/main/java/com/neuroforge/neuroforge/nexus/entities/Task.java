package com.neuroforge.neuroforge.nexus.entities;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "tasks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Task {

    @Id
    String id;

    @Indexed(unique = true)
    UUID taskId;

    @Indexed
    String projectId;

    // Denormalized from the parent project at creation time, so task
    // queries/security checks don't need an extra Project lookup, and so a
    // task can never be fetched across an organization boundary even if its
    // id is guessed/leaked.
    @Indexed
    String organizationId;

    String title;

    String description;

    @Builder.Default
    TaskStatus status = TaskStatus.TODO;

    String assigneeId;

    // Optional due date, surfaced in the calendar view on the frontend.
    LocalDate dueDate;

    String createdBy;

    @Builder.Default
    List<Subtask> subtasks = new ArrayList<>();

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}