package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document("releases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReleaseRecord {
 @Id private String id;
 @Indexed private String organizationId;
 @Indexed private String projectId;
 private String version;
 private String title;
 private String notes;
 @Builder.Default private String status="DRAFT";
 @Builder.Default private List<String> taskIds=new ArrayList<>();
 private String createdBy;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;
}
