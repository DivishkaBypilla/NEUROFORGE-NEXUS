package com.neuroforge.neuroforge.nexus.dto.request;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskStatusRequest {

    @NotNull(message = "Status is required")
    TaskStatus status;
}