package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "bug_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BugReport {
 @Id private String id;
 private String title;
 private String description;
 private String projectProduct;
 private String moduleFeature;
 private String environment;
 private String severity;
 private String priority;
 private String status;
 private String reportedById;
 private String reportedByName;
 private String assignedToId;
 private String organizationId;
 private Instant createdAt;
 @Builder.Default private List<BugAttachment> attachments = new ArrayList<>();
}
