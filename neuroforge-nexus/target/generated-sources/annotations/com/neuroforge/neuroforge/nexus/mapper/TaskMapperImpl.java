package com.neuroforge.neuroforge.nexus.mapper;

import com.neuroforge.neuroforge.nexus.dto.request.CreateTaskRequest;
import com.neuroforge.neuroforge.nexus.dto.request.UpdateTaskRequest;
import com.neuroforge.neuroforge.nexus.dto.response.SubtaskResponse;
import com.neuroforge.neuroforge.nexus.dto.response.TaskResponse;
import com.neuroforge.neuroforge.nexus.entities.Subtask;
import com.neuroforge.neuroforge.nexus.entities.Task;
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
public class TaskMapperImpl implements TaskMapper {

    @Override
    public Task toEntity(CreateTaskRequest request) {
        if ( request == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.projectId( request.getProjectId() );
        task.title( request.getTitle() );
        task.description( request.getDescription() );
        task.assigneeId( request.getAssigneeId() );
        task.dueDate( request.getDueDate() );

        return task.build();
    }

    @Override
    public TaskResponse toResponse(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskResponse.TaskResponseBuilder taskResponse = TaskResponse.builder();

        taskResponse.id( task.getId() );
        taskResponse.taskId( task.getTaskId() );
        taskResponse.projectId( task.getProjectId() );
        taskResponse.title( task.getTitle() );
        taskResponse.description( task.getDescription() );
        taskResponse.status( task.getStatus() );
        taskResponse.assigneeId( task.getAssigneeId() );
        taskResponse.dueDate( task.getDueDate() );
        taskResponse.createdBy( task.getCreatedBy() );
        taskResponse.subtasks( toSubtaskResponseList( task.getSubtasks() ) );
        taskResponse.createdAt( task.getCreatedAt() );
        taskResponse.updatedAt( task.getUpdatedAt() );

        return taskResponse.build();
    }

    @Override
    public List<TaskResponse> toResponseList(List<Task> tasks) {
        if ( tasks == null ) {
            return null;
        }

        List<TaskResponse> list = new ArrayList<TaskResponse>( tasks.size() );
        for ( Task task : tasks ) {
            list.add( toResponse( task ) );
        }

        return list;
    }

    @Override
    public SubtaskResponse toSubtaskResponse(Subtask subtask) {
        if ( subtask == null ) {
            return null;
        }

        SubtaskResponse.SubtaskResponseBuilder subtaskResponse = SubtaskResponse.builder();

        subtaskResponse.id( subtask.getId() );
        subtaskResponse.title( subtask.getTitle() );
        subtaskResponse.status( subtask.getStatus() );
        subtaskResponse.assigneeId( subtask.getAssigneeId() );
        subtaskResponse.createdAt( subtask.getCreatedAt() );
        subtaskResponse.updatedAt( subtask.getUpdatedAt() );

        return subtaskResponse.build();
    }

    @Override
    public List<SubtaskResponse> toSubtaskResponseList(List<Subtask> subtasks) {
        if ( subtasks == null ) {
            return null;
        }

        List<SubtaskResponse> list = new ArrayList<SubtaskResponse>( subtasks.size() );
        for ( Subtask subtask : subtasks ) {
            list.add( toSubtaskResponse( subtask ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromRequest(UpdateTaskRequest request, Task task) {
        if ( request == null ) {
            return;
        }

        if ( request.getTitle() != null ) {
            task.setTitle( request.getTitle() );
        }
        if ( request.getDescription() != null ) {
            task.setDescription( request.getDescription() );
        }
        if ( request.getAssigneeId() != null ) {
            task.setAssigneeId( request.getAssigneeId() );
        }
        if ( request.getDueDate() != null ) {
            task.setDueDate( request.getDueDate() );
        }
    }
}
