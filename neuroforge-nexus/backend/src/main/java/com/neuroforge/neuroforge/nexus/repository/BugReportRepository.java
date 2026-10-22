package com.neuroforge.neuroforge.nexus.repository;
import com.neuroforge.neuroforge.nexus.entities.BugReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
public interface BugReportRepository extends MongoRepository<BugReport, String> {
 List<BugReport> findAllByOrganizationIdOrderByCreatedAtDesc(String organizationId);
 Optional<BugReport> findByIdAndOrganizationId(String id, String organizationId);
}
