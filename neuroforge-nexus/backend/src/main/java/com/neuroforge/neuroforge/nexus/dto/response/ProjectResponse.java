package com.neuroforge.neuroforge.nexus.dto.response;

import com.neuroforge.neuroforge.nexus.entities.enums.ProjectPriority;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;
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
public class ProjectResponse {

    String id;

    UUID projectId;

    String name;

    String description;

    ProjectStatus status;

    ProjectPriority priority;

    String teamLead;

    String ownerId;

    List<String> memberIds;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    LocalDate startDate;

    LocalDate endDate;

    String githubRepo;
}
