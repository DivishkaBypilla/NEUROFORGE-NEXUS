package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.dto.response.SignupResponse;
import com.neuroforge.neuroforge.nexus.dto.response.UserSummaryResponse;
import com.neuroforge.neuroforge.nexus.entities.Project;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.exception.ResourceNotFoundException;
import com.neuroforge.neuroforge.nexus.mapper.UserMapper;
import com.neuroforge.neuroforge.nexus.repository.ProjectRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.CurrentUserService;
import com.neuroforge.neuroforge.nexus.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;

    @Override
    public List<SignupResponse> getAllUsers() {
        String organizationId = currentUserService.organizationId();
        return userMapper.toAllResponse(userRepository.findAllByOrganizationId(organizationId));
    }

    @Override
    public SignupResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id: " + id + " not found"));
        requireSameOrganization(user);
        return userMapper.toResponse(user);
    }

    @Override
    public void removeUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with id: " + id + " not found"));
        requireSameOrganization(user);
        userRepository.deleteById(id);
    }

    @Override
    public List<SignupResponse> getUserByRole(String role) {
        String organizationId = currentUserService.organizationId();
        List<User> users = userRepository.findAllByRole(role).stream()
                .filter(u -> organizationId.equals(u.getOrganizationId()))
                .toList();
        if (users.isEmpty()) {
            throw new RuntimeException("No users found with role: " + role);
        }
        return userMapper.toAllResponse(users);
    }

    public SignupResponse updateRole(String id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        requireSameOrganization(user);
        user.setRole(newRole);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public SignupResponse getCurrentUser(User principal) {
        // principal is already the authenticated User document (no extra DB round-trip needed)
        return userMapper.toResponse(principal);
    }

    @Override
    public List<UserSummaryResponse> getUserDirectory(User principal) {
        if (principal.getRole() == Role.ADMIN || principal.getRole() == Role.PROJECT_MANAGER) {
            return userMapper.toSummaryList(userRepository.findAllByOrganizationId(principal.getOrganizationId()));
        }

        // Not a manager: only show people who actually share a project with them
        // (owner, team lead, or a member on any project this user is involved in),
        // plus themselves. findAllInvolvingUser is already organization-scoped.
        List<Project> myProjects = projectRepository.findAllInvolvingUser(principal.getOrganizationId(), principal.getId());

        Set<String> colleagueIds = new HashSet<>();
        colleagueIds.add(principal.getId());
        for (Project project : myProjects) {
            if (project.getOwnerId() != null) {
                colleagueIds.add(project.getOwnerId());
            }
            if (project.getTeamLead() != null) {
                colleagueIds.add(project.getTeamLead());
            }
            if (project.getMemberIds() != null) {
                colleagueIds.addAll(project.getMemberIds());
            }
        }

        List<User> colleagues = new ArrayList<>();
        userRepository.findAllById(colleagueIds).stream()
                .filter(u -> principal.getOrganizationId() != null && principal.getOrganizationId().equals(u.getOrganizationId()))
                .forEach(colleagues::add);
        return userMapper.toSummaryList(colleagues);
    }

    // Defense in depth: even with a guessed/leaked id from another company's
    // data, nothing here lets you read, modify, or delete a user outside
    // your own organization.
    private void requireSameOrganization(User user) {
        String organizationId = currentUserService.organizationId();
        if (!organizationId.equals(user.getOrganizationId())) {
            throw new ResourceNotFoundException("User with id: " + user.getId() + " not found");
        }
    }
}
