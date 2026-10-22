package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.dto.request.CreateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.response.ProjectResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectPriority;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.exception.ProjectAlreadyExistsException;
import com.neuroforge.neuroforge.nexus.exception.ProjectNotFoundException;
import com.neuroforge.neuroforge.nexus.exception.ResourceNotFoundException;
import com.neuroforge.neuroforge.nexus.mapper.ProjectMapper;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.CurrentUserService;
import com.neuroforge.neuroforge.nexus.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final CurrentUserService currentUserService;

    // Looks up a user by id, rejects ADMIN accounts (admins administer the
    // platform, they aren't project team members), AND rejects anyone
    // outside the current user's organization — without this last check, a
    // PM in Company A could add a Company B user id (if guessed/leaked) as
    // a team lead or member on a Company A project.
    private User getAssignableUser(String userId, String label, String organizationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found with id: " + userId));
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    label + " cannot be an ADMIN account — admins aren't assignable as project team members");
        }
        if (!organizationId.equals(user.getOrganizationId())) {
            throw new ResourceNotFoundException(label + " not found with id: " + userId);
        }
        return user;
    }

    private Project requireProjectInOrg(String id, String organizationId) {
        return projectRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with id: " + id));
    }

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        String organizationId = currentUserService.organizationId();
        log.info("Creating new project with name: {} in organization {}", request.getName(), organizationId);

        if (projectRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new ProjectAlreadyExistsException("Project with name '" + request.getName() + "' already exists");
        }

        if (request.getOwnerId() != null) {
            getAssignableUser(request.getOwnerId(), "Owner", organizationId);
        }

        if (request.getTeamLead() != null) {
            getAssignableUser(request.getTeamLead(), "Team lead", organizationId);
        }

        if (request.getMemberIds() != null) {
            for (String memberId : request.getMemberIds()) {
                getAssignableUser(memberId, "Member", organizationId);
            }
        }

        Project project = projectMapper.toEntity(request);
        project.setProjectId(UUID.randomUUID());
        project.setOrganizationId(organizationId);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.PLANNED);
        }
        if (project.getPriority() == null) {
            project.setPriority(ProjectPriority.MEDIUM);
        }
        if (project.getMemberIds() == null) {
            project.setMemberIds(new ArrayList<>());
        }

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return projectMapper.toResponse(savedProject);
    }

    @Override
    public ProjectResponse getProjectById(String id) {
        log.info("Fetching project by ID: {}", id);
        Project project = requireProjectInOrg(id, currentUserService.organizationId());
        return projectMapper.toResponse(project);
    }

    @Override
    public List<ProjectResponse> getAllProjects() {
        String organizationId = currentUserService.organizationId();
        log.info("Fetching all projects in organization {}", organizationId);
        List<Project> projects = projectRepository.findAllByOrganizationId(organizationId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getVisibleProjects(User principal) {
        // PROJECT_MANAGER and ADMIN run/oversee their whole organization's
        // portfolio, so they see every project IN THEIR OWN ORGANIZATION.
        // Everyone else (DEVELOPER, TESTER, DEVOPS_ENGINEER, ...) only sees
        // projects they're actually enrolled in, also within their org.
        if (principal.getRole() == Role.ADMIN || principal.getRole() == Role.PROJECT_MANAGER) {
            return getAllProjects();
        }

        log.info("Fetching projects involving user ID: {} in organization {}", principal.getId(), principal.getOrganizationId());
        List<Project> projects = projectRepository.findAllInvolvingUser(principal.getOrganizationId(), principal.getId());
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsByOwner(String ownerId) {
        String organizationId = currentUserService.organizationId();
        log.info("Fetching projects owned by user ID: {} in organization {}", ownerId, organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndOwnerId(organizationId, ownerId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsByStatus(ProjectStatus status) {
        String organizationId = currentUserService.organizationId();
        log.info("Fetching projects with status: {} in organization {}", status, organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndStatus(organizationId, status);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsByMember(String memberId) {
        String organizationId = currentUserService.organizationId();
        log.info("Fetching projects containing member ID: {} in organization {}", memberId, organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndMemberIdsContaining(organizationId, memberId);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public List<ProjectResponse> getProjectsByTeamLead(String teamLead) {
        String organizationId = currentUserService.organizationId();
        log.info("Fetching projects led by team lead ID: {} in organization {}", teamLead, organizationId);
        List<Project> projects = projectRepository.findByOrganizationIdAndTeamLead(organizationId, teamLead);
        return projectMapper.toResponseList(projects);
    }

    @Override
    public ProjectResponse updateProject(String id, UpdateProjectRequest request) {
        String organizationId = currentUserService.organizationId();
        log.info("Updating project with ID: {}", id);
        Project project = requireProjectInOrg(id, organizationId);

        if (request.getName() != null && !request.getName().equals(project.getName())
                && projectRepository.existsByOrganizationIdAndName(organizationId, request.getName())) {
            throw new ProjectAlreadyExistsException("Project with name '" + request.getName() + "' already exists");
        }

        if (request.getOwnerId() != null) {
            getAssignableUser(request.getOwnerId(), "Owner", organizationId);
        }

        if (request.getTeamLead() != null) {
            getAssignableUser(request.getTeamLead(), "Team lead", organizationId);
        }

        if (request.getMemberIds() != null) {
            for (String memberId : request.getMemberIds()) {
                getAssignableUser(memberId, "Member", organizationId);
            }
        }

        projectMapper.updateEntityFromRequest(request, project);
        project.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully with ID: {}", updatedProject.getId());

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public void deleteProject(String id) {
        String organizationId = currentUserService.organizationId();
        log.info("Deleting project with ID: {}", id);
        requireProjectInOrg(id, organizationId); // throws 404 if it's not this org's project
        projectRepository.deleteById(id);
        log.info("Project deleted successfully with ID: {}", id);
    }

    @Override
    public ProjectResponse addMemberToProject(String projectId, String memberId) {
        String organizationId = currentUserService.organizationId();
        log.info("Adding member ID {} to project ID {}", memberId, projectId);
        Project project = requireProjectInOrg(projectId, organizationId);

        getAssignableUser(memberId, "Member", organizationId);

        if (project.getMemberIds() == null) {
            project.setMemberIds(new ArrayList<>());
        }

        if (!project.getMemberIds().contains(memberId)) {
            project.getMemberIds().add(memberId);
            project.setUpdatedAt(LocalDateTime.now());
            project = projectRepository.save(project);
        }

        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse removeMemberFromProject(String projectId, String memberId) {
        String organizationId = currentUserService.organizationId();
        log.info("Removing member ID {} from project ID {}", memberId, projectId);
        Project project = requireProjectInOrg(projectId, organizationId);

        if (project.getMemberIds() != null && project.getMemberIds().contains(memberId)) {
            project.getMemberIds().remove(memberId);
            project.setUpdatedAt(LocalDateTime.now());
            project = projectRepository.save(project);
        }

        return projectMapper.toResponse(project);
    }
}
