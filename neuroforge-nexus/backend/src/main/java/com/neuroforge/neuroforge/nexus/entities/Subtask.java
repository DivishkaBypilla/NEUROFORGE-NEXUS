package com.neuroforge.neuroforge.nexus.entities;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

// Embedded inside a Task document (its own collection isn't needed — subtasks
// are always addressed through their parent task).
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subtask {

    // Not a Mongo @Id (this class isn't a top-level @Document) — just a stable
    // string generated at creation time so one subtask in the list is addressable.
    String id;

    String title;

    @Builder.Default
    TaskStatus status = TaskStatus.TODO;

    String assigneeId;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}