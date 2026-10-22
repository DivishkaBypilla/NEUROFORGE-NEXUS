package com.neuroforge.neuroforge.nexus.repository;
import com.neuroforge.neuroforge.nexus.entities.ReleaseRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
public interface ReleaseRecordRepository extends MongoRepository<ReleaseRecord,String> { List<ReleaseRecord> findAllByProjectIdAndOrganizationIdOrderByCreatedAtDesc(String projectId,String organizationId); }
