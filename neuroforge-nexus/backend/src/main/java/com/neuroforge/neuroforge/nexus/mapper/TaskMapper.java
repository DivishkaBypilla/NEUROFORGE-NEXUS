package com.neuroforge.neuroforge.nexus.mapper;

import com.neuroforge.neuroforge.nexus.dto.request.CreateTaskRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateTaskRequest;
import com.neuroforge.neuroforge.nexus.dto.response.SubtaskResponse;
import com.neuroforge.neuroforge.nexus.dto.response.TaskResponse;
import com.neuroforge.neuroforge.nexus.entities.Subtask;
import com.neuroforge.neuroforge.nexus.entities.Task;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    // status/createdBy/subtasks/taskId are all set explicitly in the service
    // (status may default or come from the request; the rest can't come from
    // the request at all), so they're ignored here rather than auto-mapped.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "subtasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Task toEntity(CreateTaskRequest request);

    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);

    SubtaskResponse toSubtaskResponse(Subtask subtask);

    List<SubtaskResponse> toSubtaskResponseList(List<Subtask> subtasks);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "subtasks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateTaskRequest request, @MappingTarget Task task);
}