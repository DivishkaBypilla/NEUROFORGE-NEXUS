package com.neuroforge.neuroforge.nexus.dto.response;

import com.neuroforge.neuroforge.nexus.entities.enums.TaskStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubtaskResponse {

    String id;

    String title;

    TaskStatus status;

    String assigneeId;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}