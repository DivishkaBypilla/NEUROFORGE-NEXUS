package com.neuroforge.neuroforge.nexus.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuroforge.neuroforge.nexus.entities.*;
import com.neuroforge.neuroforge.nexus.repository.BugReportRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/bugs") @RequiredArgsConstructor
public class BugReportController {
 private final BugReportRepository bugs; private final CurrentUserService currentUser; private final UserRepository users; private final ObjectMapper mapper;
 @Value("${neuroforge.upload-dir:./data/bug-attachments}") private String uploadDir;
 private static final Set<String> ENV=Set.of("Development","Staging","Production");
 private static final Set<String> SEVERITY=Set.of("Critical","High","Medium","Low");
 private static final Set<String> PRIORITY=Set.of("Urgent","High","Medium","Low");
 private static final Set<String> STATUS=Set.of("New","Triaged","Assigned","In Progress","Fixed","Retest","Closed","Reopened");
 @GetMapping public List<BugReport> list(){ return bugs.findAllByOrganizationIdOrderByCreatedAtDesc(currentUser.organizationId()); }
 @GetMapping("/{id}") public BugReport get(@PathVariable String id){ return find(id); }
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
 public ResponseEntity<BugReport> create(@RequestPart("report") String json, @RequestPart(value="files", required=false) List<MultipartFile> files) throws Exception {
  User user=currentUser.require(); BugReport r=mapper.readValue(json,BugReport.class); validate(r);
  r.setId(null); r.setOrganizationId(user.getOrganizationId()); r.setReportedById(user.getId()); r.setReportedByName(user.getName()); r.setCreatedAt(Instant.now());
  if(r.getStatus()==null || r.getStatus().isBlank()) r.setStatus("New");
  r.setAttachments(new ArrayList<>()); BugReport saved=bugs.save(r);
  if(files!=null) for(MultipartFile f:files){ if(f.isEmpty()) continue; if(f.getSize()>15L*1024*1024) throw new IllegalArgumentException("Each attachment must be 15 MB or smaller");
   String original=Path.of(Objects.requireNonNullElse(f.getOriginalFilename(),"attachment")).getFileName().toString(); String storage=UUID.randomUUID().toString(); Path dir=Path.of(uploadDir).toAbsolutePath().normalize(); Files.createDirectories(dir); Files.copy(f.getInputStream(),dir.resolve(storage),StandardCopyOption.REPLACE_EXISTING);
   saved.getAttachments().add(BugAttachment.builder().id(UUID.randomUUID().toString()).originalName(original).contentType(Objects.requireNonNullElse(f.getContentType(),"application/octet-stream")).size(f.getSize()).storageName(storage).uploadedAtIso(Instant.now().toString()).build());
  }
  return ResponseEntity.status(HttpStatus.CREATED).body(bugs.save(saved));
 }
 @PatchMapping("/{id}") public BugReport update(@PathVariable String id,@RequestBody BugReport changes){ BugReport r=find(id); if(changes.getStatus()!=null){ if(!STATUS.contains(changes.getStatus())) throw new IllegalArgumentException("Invalid bug status"); String from=Objects.requireNonNullElse(r.getStatus(),"New"), to=changes.getStatus(); Map<String,Set<String>> transitions=Map.of("New",Set.of("Triaged"),"Triaged",Set.of("Assigned","In Progress"),"Assigned",Set.of("In Progress"),"In Progress",Set.of("Fixed"),"Fixed",Set.of("Retest"),"Retest",Set.of("Closed","Reopened"),"Reopened",Set.of("In Progress"),"Closed",Set.of("Reopened")); if(!from.equals(to)&&!transitions.getOrDefault(from,Set.of()).contains(to)) throw new IllegalArgumentException("Invalid workflow transition: "+from+" → "+to); r.setStatus(to); } if(changes.getAssignedToId()!=null){ if(!changes.getAssignedToId().isBlank()){ User assignee=users.findById(changes.getAssignedToId()).orElseThrow(()->new IllegalArgumentException("Assignee not found")); if(!Objects.equals(assignee.getOrganizationId(),r.getOrganizationId())) throw new AccessDeniedException("Assignee must belong to your organization"); } r.setAssignedToId(changes.getAssignedToId()); } return bugs.save(r); }
 @GetMapping("/{id}/attachments/{attachmentId}") public ResponseEntity<byte[]> attachment(@PathVariable String id,@PathVariable String attachmentId) throws Exception { BugReport r=find(id); BugAttachment a=r.getAttachments().stream().filter(x->x.getId().equals(attachmentId)).findFirst().orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND)); Path file=Path.of(uploadDir).toAbsolutePath().normalize().resolve(a.getStorageName()).normalize(); if(!file.startsWith(Path.of(uploadDir).toAbsolutePath().normalize())||!Files.exists(file)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND); return ResponseEntity.ok().contentType(MediaType.parseMediaType(a.getContentType())).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(a.getOriginalName()).build().toString()).body(Files.readAllBytes(file)); }
 private BugReport find(String id){ return bugs.findByIdAndOrganizationId(id,currentUser.organizationId()).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Bug report not found")); }
 private void validate(BugReport r){ if(r.getTitle()==null||r.getTitle().isBlank()) throw new IllegalArgumentException("Bug title is required"); if(r.getDescription()==null||r.getDescription().isBlank()) throw new IllegalArgumentException("Description is required"); if(r.getEnvironment()!=null&&!ENV.contains(r.getEnvironment())) throw new IllegalArgumentException("Invalid environment"); if(r.getSeverity()!=null&&!SEVERITY.contains(r.getSeverity())) throw new IllegalArgumentException("Invalid severity"); if(r.getPriority()!=null&&!PRIORITY.contains(r.getPriority())) throw new IllegalArgumentException("Invalid priority"); if(r.getStatus()!=null&&!STATUS.contains(r.getStatus())) throw new IllegalArgumentException("Invalid status"); }
}
