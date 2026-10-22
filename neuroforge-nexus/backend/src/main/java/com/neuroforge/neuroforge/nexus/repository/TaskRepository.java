package com.neuroforge.neuroforge.nexus.repository;

import com.neuroforge.neuroforge.nexus.entities.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByProjectId(String projectId);

    List<Task> findByAssigneeId(String assigneeId);

    List<Task> findByProjectIdAndAssigneeId(String projectId, String assigneeId);

    // Organization-scoped variants — the actual tenant-isolation boundary.
    // A task id from another organization simply doesn't match and resolves
    // to empty, the same as an unknown id.
    Optional<Task> findByIdAndOrganizationId(String id, String organizationId);

    List<Task> findByProjectIdAndOrganizationId(String projectId, String organizationId);

    List<Task> findByProjectIdAndAssigneeIdAndOrganizationId(String projectId, String assigneeId, String organizationId);

    List<Task> findByAssigneeIdAndOrganizationId(String assigneeId, String organizationId);

    boolean existsByIdAndOrganizationId(String id, String organizationId);
}
