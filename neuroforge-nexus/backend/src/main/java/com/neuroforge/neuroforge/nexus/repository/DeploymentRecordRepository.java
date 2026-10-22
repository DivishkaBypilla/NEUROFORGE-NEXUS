package com.neuroforge.neuroforge.nexus.repository;
import com.neuroforge.neuroforge.nexus.entities.DeploymentRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface DeploymentRecordRepository extends MongoRepository<DeploymentRecord,String> { List<DeploymentRecord> findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(String projectId,String organizationId); }
