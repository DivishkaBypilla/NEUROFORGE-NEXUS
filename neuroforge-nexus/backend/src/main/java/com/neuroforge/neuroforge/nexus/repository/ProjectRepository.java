package com.neuroforge.neuroforge.nexus.repository;

import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.enums.ProjectStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    boolean existsByOrganizationIdAndName(String organizationId, String name);

    Optional<Project> findByProjectId(UUID projectId);

    // Scoped-by-id lookup for anything resolved from a path variable — this
    // is what actually enforces the tenant boundary: a project id from
    // another organization simply doesn't match and resolves to empty,
    // exactly like a 404 for an unknown id.
    Optional<Project> findByIdAndOrganizationId(String id, String organizationId);

    List<Project> findAllByOrganizationId(String organizationId);

    List<Project> findByOrganizationIdAndOwnerId(String organizationId, String ownerId);

    List<Project> findByOrganizationIdAndStatus(String organizationId, ProjectStatus status);

    List<Project> findByOrganizationIdAndMemberIdsContaining(String organizationId, String memberId);

    List<Project> findByOrganizationIdAndTeamLead(String organizationId, String teamLead);

    @Query("{ 'organizationId': ?0, '$or': [ { 'ownerId': ?1 }, { 'teamLead': ?1 }, { 'memberIds': ?1 } ] }")
    List<Project> findAllInvolvingUser(String organizationId, String userId);
}
