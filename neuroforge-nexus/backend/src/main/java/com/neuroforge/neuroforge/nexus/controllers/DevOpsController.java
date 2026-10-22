package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.entities.*;
import com.neuroforge.neuroforge.nexus.repository.*;
import com.neuroforge.neuroforge.nexus.security.TaskSecurity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class DevOpsController {
 private final ProjectRepository projects;
 private final TaskRepository tasks;
 private final SprintRepository sprints;
 private final ReleaseRecordRepository releases;
 private final DeploymentRecordRepository deployments;
 private final TaskSecurity taskSecurity;

 public record SprintRequest(@NotBlank String name, String goal, java.time.LocalDate startDate, java.time.LocalDate endDate, List<String> taskIds) {}
 public record ReleaseRequest(@NotBlank String version, @NotBlank String title, String notes, List<String> taskIds) {}
 public record DeploymentRequest(String releaseId, @NotBlank String version, @NotBlank String environment, String source, String commitSha, String workflowUrl, String logs, @NotBlank String status) {}

 private Project project(String id, User u, boolean manage) {
  Project p=projects.findByIdAndOrganizationId(id,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found"));
  org.springframework.security.core.Authentication auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
  boolean allowed=manage ? taskSecurity.canManageProjectTasks(id,auth) : taskSecurity.canViewProject(id,auth);
  if(!allowed) throw new AccessDeniedException("You do not have access to this project");
  return p;
 }
 private void validateTasks(String projectId, User u, List<String> ids) {
  if(ids==null)return;
  for(String id:new HashSet<>(ids)) {
   Task t=tasks.findByIdAndOrganizationId(id,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task not found in this organization: "+id));
   if(!projectId.equals(t.getProjectId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Every sprint/release task must belong to this project");
  }
 }

 @GetMapping("/projects/{projectId}/sprints")
 public List<Sprint> listSprints(@PathVariable String projectId,@AuthenticationPrincipal User u){ project(projectId,u,false); return sprints.findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(projectId,u.getOrganizationId()); }
 @PostMapping("/projects/{projectId}/sprints") @ResponseStatus(HttpStatus.CREATED)
 public Sprint createSprint(@PathVariable String projectId,@Valid @RequestBody SprintRequest r,@AuthenticationPrincipal User u){ project(projectId,u,true); dates(r.startDate(),r.endDate()); validateTasks(projectId,u,r.taskIds()); LocalDateTime now=LocalDateTime.now(); return sprints.save(Sprint.builder().organizationId(u.getOrganizationId()).projectId(projectId).name(r.name().trim()).goal(r.goal()).startDate(r.startDate()).endDate(r.endDate()).status("PLANNED").taskIds(r.taskIds()==null?new ArrayList<>():new ArrayList<>(new LinkedHashSet<>(r.taskIds()))).createdBy(u.getId()).createdAt(now).updatedAt(now).build()); }
 @PutMapping("/projects/{projectId}/sprints/{sprintId}")
 public Sprint updateSprint(@PathVariable String projectId,@PathVariable String sprintId,@Valid @RequestBody SprintRequest r,@AuthenticationPrincipal User u){ project(projectId,u,true); dates(r.startDate(),r.endDate()); validateTasks(projectId,u,r.taskIds()); Sprint s=sprints.findByIdAndProjectIdAndOrganizationId(sprintId,projectId,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Sprint not found")); s.setName(r.name().trim());s.setGoal(r.goal());s.setStartDate(r.startDate());s.setEndDate(r.endDate());if(r.taskIds()!=null)s.setTaskIds(new ArrayList<>(new LinkedHashSet<>(r.taskIds())));s.setUpdatedAt(LocalDateTime.now());return sprints.save(s); }
 @PatchMapping("/projects/{projectId}/sprints/{sprintId}/status")
 public Sprint sprintStatus(@PathVariable String projectId,@PathVariable String sprintId,@RequestBody Map<String,String> body,@AuthenticationPrincipal User u){project(projectId,u,true);String status=body.getOrDefault("status","").toUpperCase(Locale.ROOT);if(!Set.of("PLANNED","ACTIVE","COMPLETED","CANCELLED").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid sprint status");Sprint s=sprints.findByIdAndProjectIdAndOrganizationId(sprintId,projectId,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Sprint not found"));if("ACTIVE".equals(status)&&sprints.findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(projectId,u.getOrganizationId()).stream().anyMatch(x->!x.getId().equals(sprintId)&&"ACTIVE".equals(x.getStatus())))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only one sprint may be active per project");s.setStatus(status);s.setUpdatedAt(LocalDateTime.now());return sprints.save(s);}
 @PostMapping("/projects/{projectId}/sprints/{sprintId}/tasks/{taskId}")
 public Sprint addTaskToSprint(@PathVariable String projectId,@PathVariable String sprintId,@PathVariable String taskId,@AuthenticationPrincipal User u){project(projectId,u,true);Task task=tasks.findByIdAndOrganizationId(taskId,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Task not found"));if(!projectId.equals(task.getProjectId()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Task belongs to a different project");Sprint s=sprints.findByIdAndProjectIdAndOrganizationId(sprintId,projectId,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Sprint not found"));if(!s.getTaskIds().contains(taskId))s.getTaskIds().add(taskId);s.setUpdatedAt(LocalDateTime.now());return sprints.save(s);}
 @DeleteMapping("/projects/{projectId}/sprints/{sprintId}/tasks/{taskId}")
 public Sprint removeTaskFromSprint(@PathVariable String projectId,@PathVariable String sprintId,@PathVariable String taskId,@AuthenticationPrincipal User u){project(projectId,u,true);Sprint s=sprints.findByIdAndProjectIdAndOrganizationId(sprintId,projectId,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Sprint not found"));s.getTaskIds().remove(taskId);s.setUpdatedAt(LocalDateTime.now());return sprints.save(s);}

 @DeleteMapping("/projects/{projectId}/sprints/{sprintId}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void deleteSprint(@PathVariable String projectId,@PathVariable String sprintId,@AuthenticationPrincipal User u){project(projectId,u,true);Sprint s=sprints.findByIdAndProjectIdAndOrganizationId(sprintId,projectId,u.getOrganizationId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Sprint not found"));if("ACTIVE".equals(s.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Complete or cancel an active sprint before deleting it");sprints.delete(s);}

 @GetMapping("/projects/{projectId}/releases") public List<ReleaseRecord> listReleases(@PathVariable String projectId,@AuthenticationPrincipal User u){project(projectId,u,false);return releases.findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(projectId,u.getOrganizationId());}
 @PostMapping("/projects/{projectId}/releases") @ResponseStatus(HttpStatus.CREATED) public ReleaseRecord createRelease(@PathVariable String projectId,@Valid @RequestBody ReleaseRequest r,@AuthenticationPrincipal User u){project(projectId,u,true);validateTasks(projectId,u,r.taskIds());LocalDateTime now=LocalDateTime.now();return releases.save(ReleaseRecord.builder().organizationId(u.getOrganizationId()).projectId(projectId).version(r.version().trim()).title(r.title().trim()).notes(r.notes()).taskIds(r.taskIds()==null?new ArrayList<>():new ArrayList<>(new LinkedHashSet<>(r.taskIds()))).status("DRAFT").createdBy(u.getId()).createdAt(now).updatedAt(now).build());}
 @PatchMapping("/projects/{projectId}/releases/{releaseId}/status") public ReleaseRecord releaseStatus(@PathVariable String projectId,@PathVariable String releaseId,@RequestBody Map<String,String> body,@AuthenticationPrincipal User u){project(projectId,u,true);String status=body.getOrDefault("status","").toUpperCase(Locale.ROOT);if(!Set.of("DRAFT","READY","RELEASED","CANCELLED").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid release status");ReleaseRecord r=releases.findById(releaseId).filter(x->projectId.equals(x.getProjectId())&&u.getOrganizationId().equals(x.getOrganizationId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Release not found"));r.setStatus(status);r.setUpdatedAt(LocalDateTime.now());return releases.save(r);}

 @GetMapping("/projects/{projectId}/deployments") public List<DeploymentRecord> listDeployments(@PathVariable String projectId,@AuthenticationPrincipal User u){project(projectId,u,false);return deployments.findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(projectId,u.getOrganizationId());}
 @PostMapping("/projects/{projectId}/deployments") @ResponseStatus(HttpStatus.CREATED) public DeploymentRecord createDeployment(@PathVariable String projectId,@Valid @RequestBody DeploymentRequest r,@AuthenticationPrincipal User u){project(projectId,u,true);String env=r.environment().toUpperCase(Locale.ROOT),status=r.status().toUpperCase(Locale.ROOT);if(!Set.of("DEVELOPMENT","STAGING","PRODUCTION").contains(env))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid environment");if(!Set.of("PENDING","IN_PROGRESS","SUCCESS","FAILED","ROLLED_BACK").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid deployment status");if(r.releaseId()!=null&&!r.releaseId().isBlank()){ReleaseRecord rel=releases.findById(r.releaseId()).filter(x->projectId.equals(x.getProjectId())&&u.getOrganizationId().equals(x.getOrganizationId())).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Release not found in this project"));if("PRODUCTION".equals(env)&&!"READY".equals(rel.getStatus())&& !"RELEASED".equals(rel.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Release must be Ready before production deployment");}
 LocalDateTime now=LocalDateTime.now();return deployments.save(DeploymentRecord.builder().organizationId(u.getOrganizationId()).projectId(projectId).releaseId(r.releaseId()).version(r.version().trim()).environment(env).status(status).source(r.source()).commitSha(r.commitSha()).workflowUrl(r.workflowUrl()).logs(r.logs()).initiatedBy(u.getId()).startedAt(now).finishedAt(Set.of("SUCCESS","FAILED","ROLLED_BACK").contains(status)?now:null).createdAt(now).build());}
 @PatchMapping("/projects/{projectId}/deployments/{deploymentId}/status") public DeploymentRecord updateDeploymentStatus(@PathVariable String projectId,@PathVariable String deploymentId,@RequestBody Map<String,String> body,@AuthenticationPrincipal User u){project(projectId,u,true);String status=body.getOrDefault("status","").toUpperCase(Locale.ROOT);if(!Set.of("PENDING","IN_PROGRESS","SUCCESS","FAILED","ROLLED_BACK").contains(status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid deployment status");DeploymentRecord d=deployments.findById(deploymentId).filter(x->projectId.equals(x.getProjectId())&&u.getOrganizationId().equals(x.getOrganizationId())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Deployment not found"));d.setStatus(status);if(Set.of("SUCCESS","FAILED","ROLLED_BACK").contains(status))d.setFinishedAt(LocalDateTime.now());return deployments.save(d);}
 private void dates(java.time.LocalDate start,java.time.LocalDate end){if(start!=null&&end!=null&&end.isBefore(start))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Sprint end date must be on or after its start date");}
}
