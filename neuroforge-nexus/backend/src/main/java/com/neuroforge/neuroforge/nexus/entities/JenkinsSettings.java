package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/** Jenkins credentials are stored per organization, never globally shared. */
@Document(collection = "jenkins_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JenkinsSettings {
    @Id String id; // organizationId
    String baseUrl;
    String username;
    String apiToken;
    String defaultJob;
    LocalDateTime updatedAt;
    String updatedBy;
}
