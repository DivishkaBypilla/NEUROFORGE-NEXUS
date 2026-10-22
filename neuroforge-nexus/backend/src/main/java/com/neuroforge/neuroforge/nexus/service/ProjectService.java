package com.neuroforge.neuroforge.nexus.service;

import com.neuroforge.neuroforge.nexus.dto.request.CreateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.response.ProjectResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectById(String id);

    List<ProjectResponse> getAllProjects();

    List<ProjectResponse> getProjectsByOwner(String ownerId);

    List<ProjectResponse> getVisibleProjects(User principal);

    List<ProjectResponse> getProjectsByStatus(ProjectStatus status);

    List<ProjectResponse> getProjectsByMember(String memberId);

    ProjectResponse updateProject(String id, UpdateProjectRequest request);

    void deleteProject(String id);

    ProjectResponse addMemberToProject(String projectId, String memberId);

    ProjectResponse removeMemberFromProject(String projectId, String memberId);

    List<ProjectResponse> getProjectsByTeamLead(String teamLead);
}
