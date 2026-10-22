package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document("deployments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeploymentRecord {
 @Id private String id;
 @Indexed private String organizationId;
 @Indexed private String projectId;
 private String releaseId;
 private String version;
 private String environment;
 @Builder.Default private String status="PENDING";
 private String source;
 private String commitSha;
 private String workflowUrl;
 private String logs;
 private String initiatedBy;
 private LocalDateTime startedAt;
 private LocalDateTime finishedAt;
 private LocalDateTime createdAt;
}
