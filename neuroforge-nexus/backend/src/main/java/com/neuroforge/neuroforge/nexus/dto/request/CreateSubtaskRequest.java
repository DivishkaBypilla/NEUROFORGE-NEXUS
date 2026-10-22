package com.neuroforge.neuroforge.nexus.dto.request;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSubtaskRequest {

    @NotBlank(message = "Subtask title is required")
    String title;

    String assigneeId;

    TaskStatus status;
}