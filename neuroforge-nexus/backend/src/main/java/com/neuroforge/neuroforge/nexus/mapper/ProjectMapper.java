package com.neuroforge.neuroforge.nexus.mapper;

import com.neuroforge.neuroforge.nexus.dto.request.CreateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateProjectRequest;
import com.neuroforge.neuroforge.nexus.dto.response.ProjectResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toEntity(CreateProjectRequest request);

    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateProjectRequest request, @MappingTarget Project project);
}
