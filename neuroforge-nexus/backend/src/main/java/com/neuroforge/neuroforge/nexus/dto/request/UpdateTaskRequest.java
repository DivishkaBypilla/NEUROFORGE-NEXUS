package com.neuroforge.neuroforge.nexus.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

// Status is deliberately not here — status changes go through the dedicated
// PATCH /tasks/{id}/status endpoint, which has its own (looser) permission
// check. Keeping it out of the general edit request avoids blurring that line.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskRequest {

    String title;

    String description;

    String assigneeId;

    LocalDate dueDate;
}