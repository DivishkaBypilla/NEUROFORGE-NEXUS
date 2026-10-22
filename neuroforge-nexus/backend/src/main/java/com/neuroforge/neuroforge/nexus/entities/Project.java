package com.neuroforge.neuroforge.nexus.entities;

import com.neuroforge.neuroforge.nexus.entities.enums.ProjectPriority;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "projects")
// Project names only need to be unique *within* an organization — two
// different companies sharing this database can both have a "Website
// Redesign" project. (Previously `name` alone was globally unique, which
// was itself a multi-tenancy bug.)
@CompoundIndexes({
        @CompoundIndex(name = "org_name_unique", def = "{'organizationId': 1, 'name': 1}", unique = true)
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Project {

    @Id
    String id;

    @Indexed(unique = true)
    UUID projectId;

    String name;

    String description;

    // Every project belongs to exactly one organization (tenant), set from
    // the creating user's own organization — never client-supplied.
    @Indexed
    String organizationId;

    String teamLead;

    @Builder.Default
    ProjectStatus status = ProjectStatus.PLANNED;

    @Builder.Default
    ProjectPriority priority = ProjectPriority.MEDIUM;

    String ownerId;

    @Builder.Default
    List<String> memberIds = new ArrayList<>();

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    LocalDate startDate;

    LocalDate endDate;

    // "owner/repo" of the GitHub repository linked to this project. Optional
    // — null until someone links a repo from the project settings.
    String githubRepo;
}
