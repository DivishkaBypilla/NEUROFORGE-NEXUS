package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document("sprints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@CompoundIndex(name="org_project_sprint_name", def="{'organizationId':1,'projectId':1,'name':1}")
public class Sprint {
 @Id private String id;
 @Indexed private String organizationId;
 @Indexed private String projectId;
 private String name;
 private String goal;
 private LocalDate startDate;
 private LocalDate endDate;
 @Builder.Default private String status="PLANNED";
 @Builder.Default private List<String> taskIds=new ArrayList<>();
 private String createdBy;
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;
}
