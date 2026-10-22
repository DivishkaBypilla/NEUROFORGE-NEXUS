package com.neuroforge.neuroforge.nexus.entities;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BugAttachment {
 private String id; private String originalName; private String contentType; private long size; private String storageName; // String ISO timestamp avoids serialization ambiguity across clients.
 private String uploadedAtIso;
}
