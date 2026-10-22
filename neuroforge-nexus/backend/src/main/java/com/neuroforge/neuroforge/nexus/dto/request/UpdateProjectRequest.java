package com.neuroforge.neuroforge.nexus.dto.request;

import com.neuroforge.neuroforge.nexus.entities.enums.ProjectPriority;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProjectRequest {

    String name;

    String description;

    ProjectStatus status;

    ProjectPriority priority;

    String teamLead;

    String ownerId;

    List<String> memberIds;

    LocalDate startDate;

    LocalDate endDate;

    String githubRepo;
}
