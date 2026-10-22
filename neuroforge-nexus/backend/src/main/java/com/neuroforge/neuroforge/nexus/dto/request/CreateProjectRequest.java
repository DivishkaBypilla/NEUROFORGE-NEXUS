package com.neuroforge.neuroforge.nexus.dto.request;

import com.neuroforge.neuroforge.nexus.entities.enums.ProjectPriority;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    String name;

    String description;

    ProjectStatus status;

    String teamLead;

    ProjectPriority priority;

    @NotBlank(message = "Owner ID is required")
    String ownerId;

    List<String> memberIds;

    LocalDate startDate;

    LocalDate endDate;

    String githubRepo;
}
