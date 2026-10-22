package com.neuroforge.neuroforge.nexus.mapper;

import com.neuroforge.neuroforge.nexus.dto.request.CreateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.response.ProjectResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T18:34:45+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public Project toEntity(CreateProjectRequest request) {
        if ( request == null ) {
            return null;
        }

        Project.ProjectBuilder project = Project.builder();

        project.name( request.getName() );
        project.description( request.getDescription() );
        project.teamLead( request.getTeamLead() );
        project.status( request.getStatus() );
        project.priority( request.getPriority() );
        project.ownerId( request.getOwnerId() );
        List<String> list = request.getMemberIds();
        if ( list != null ) {
            project.memberIds( new ArrayList<String>( list ) );
        }
        project.startDate( request.getStartDate() );
        project.endDate( request.getEndDate() );
        project.githubRepo( request.getGithubRepo() );

        return project.build();
    }

    @Override
    public ProjectResponse toResponse(Project project) {
        if ( project == null ) {
            return null;
        }

        ProjectResponse.ProjectResponseBuilder projectResponse = ProjectResponse.builder();

        projectResponse.id( project.getId() );
        projectResponse.projectId( project.getProjectId() );
        projectResponse.name( project.getName() );
        projectResponse.description( project.getDescription() );
        projectResponse.status( project.getStatus() );
        projectResponse.priority( project.getPriority() );
        projectResponse.teamLead( project.getTeamLead() );
        projectResponse.ownerId( project.getOwnerId() );
        List<String> list = project.getMemberIds();
        if ( list != null ) {
            projectResponse.memberIds( new ArrayList<String>( list ) );
        }
        projectResponse.createdAt( project.getCreatedAt() );
        projectResponse.updatedAt( project.getUpdatedAt() );
        projectResponse.startDate( project.getStartDate() );
        projectResponse.endDate( project.getEndDate() );
        projectResponse.githubRepo( project.getGithubRepo() );

        return projectResponse.build();
    }

    @Override
    public List<ProjectResponse> toResponseList(List<Project> projects) {
        if ( projects == null ) {
            return null;
        }

        List<ProjectResponse> list = new ArrayList<ProjectResponse>( projects.size() );
        for ( Project project : projects ) {
            list.add( toResponse( project ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromRequest(UpdateProjectRequest request, Project project) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            project.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            project.setDescription( request.getDescription() );
        }
        if ( request.getTeamLead() != null ) {
            project.setTeamLead( request.getTeamLead() );
        }
        if ( request.getStatus() != null ) {
            project.setStatus( request.getStatus() );
        }
        if ( request.getPriority() != null ) {
            project.setPriority( request.getPriority() );
        }
        if ( request.getOwnerId() != null ) {
            project.setOwnerId( request.getOwnerId() );
        }
        if ( project.getMemberIds() != null ) {
            List<String> list = request.getMemberIds();
            if ( list != null ) {
                project.getMemberIds().clear();
                project.getMemberIds().addAll( list );
            }
        }
        else {
            List<String> list = request.getMemberIds();
            if ( list != null ) {
                project.setMemberIds( new ArrayList<String>( list ) );
            }
        }
        if ( request.getStartDate() != null ) {
            project.setStartDate( request.getStartDate() );
        }
        if ( request.getEndDate() != null ) {
            project.setEndDate( request.getEndDate() );
        }
        if ( request.getGithubRepo() != null ) {
            project.setGithubRepo( request.getGithubRepo() );
        }
    }
}
