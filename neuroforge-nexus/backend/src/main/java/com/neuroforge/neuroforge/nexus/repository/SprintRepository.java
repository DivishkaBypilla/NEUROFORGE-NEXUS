package com.neuroforge.neuroforge.nexus.repository;
import com.neuroforge.neuroforge.nexus.entities.Sprint;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface SprintRepository extends MongoRepository<Sprint,String> {
 List<Sprint> findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(String projectId,String organizationId);
 Optional<Sprint> findByIdAndProjectIdAndOrganizationId(String id,String projectId,String organizationId);
 boolean existsByProjectIdAndOrganizationIdAndTaskIdsContaining(String projectId,String organizationId,String taskId);
}
