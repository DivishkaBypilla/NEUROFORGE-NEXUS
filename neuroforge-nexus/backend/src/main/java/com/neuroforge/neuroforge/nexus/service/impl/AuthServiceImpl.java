package com.neuroforge.neuroforge.nexus.service.impl;

import com.neuroforge.neuroforge.nexus.dto.request.LoginRequest;
import com.neuroforge.neuroforge.nexus.dto.request.SignupRequest;
import com.neuroforge.neuroforge.nexus.dto.response.LoginResponse;
import com.neuroforge.neuroforge.nexus.dto.response.SignupResponse;
import com.neuroforge.neuroforge.nexus.entities.Organization;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.exception.ResourceNotFoundException;
import com.neuroforge.neuroforge.nexus.repository.OrganizationRepository;
import com.neuroforge.neuroforge.nexus.repository.UserRepository;
import com.neuroforge.neuroforge.nexus.security.JwtUtil;
import com.neuroforge.neuroforge.nexus.service.AuthService;
import com.neuroforge.neuroforge.nexus.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.neuroforge.neuroforge.nexus.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    OrganizationRepository organizationRepository;
    UserMapper userMapper;
    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;
    PasswordEncoder passwordEncoder;
    UserService userService;

    @Override
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.email())) {
            log.error("User already exists with email {}", signupRequest.email());
            throw new RuntimeException("Email already registered");
        }

        boolean creatingOrg = signupRequest.organizationName() != null && !signupRequest.organizationName().isBlank();
        boolean joiningOrg = signupRequest.organizationSlug() != null && !signupRequest.organizationSlug().isBlank();

        if (creatingOrg == joiningOrg) {
            // Both set, or neither set — exactly one is required.
            throw new IllegalArgumentException(
                    "Provide either organizationName (to create a new organization) or organizationSlug (to join an existing one) — not both, not neither.");
        }

        String organizationId;
        Role role;

        if (creatingOrg) {
            Organization org = createOrganization(signupRequest.organizationName());
            organizationId = org.getId();
            role = Role.ADMIN; // first member of a new organization administers it
        } else {
            Organization org = organizationRepository.findBySlug(signupRequest.organizationSlug().trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No organization found with slug: " + signupRequest.organizationSlug()));
            organizationId = org.getId();
            role = Role.DEVELOPER;
        }

        User user = userMapper.toEntity(signupRequest);
        user.setUserId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(signupRequest.password()));
        user.setRole(role);
        user.setOrganizationId(organizationId);

        User savedUser = userRepository.save(user);
        log.info("Signup success with email: {} in organization {}", signupRequest.email(), organizationId);

        return userMapper.toResponse(savedUser);
    }

    private Organization createOrganization(String name) {
        String slug = slugify(name);
        String uniqueSlug = slug;
        int suffix = 2;
        while (organizationRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = slug + "-" + suffix++;
        }

        Organization org = Organization.builder()
                .name(name.trim())
                .slug(uniqueSlug)
                .createdAt(LocalDateTime.now())
                .build();

        Organization saved = organizationRepository.save(org);
        log.info("Created organization '{}' with slug '{}'", saved.getName(), saved.getSlug());
        return saved;
    }

    private String slugify(String name) {
        String base = name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        return base.isBlank() ? "org" : base;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found with email: "+request.email()));

        UUID userId = user.getUserId();
        String accessToken = jwtUtil.generateAccessToken(
                request.email(),
                user.getUserId(),
                user.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(userId);

        return new LoginResponse(userId, accessToken, refreshToken);

    }

    @Override
    public SignupResponse createUserWithRole(SignupRequest signupRequest, Role role, String organizationId) {
        if (userRepository.existsByEmail(signupRequest.email())) {
            log.error("User already exists with email {}", signupRequest.email());
            throw new RuntimeException("Email already registered");
        }
        User user = userMapper.toEntity(signupRequest);
        user.setUserId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(signupRequest.password()));
        user.setRole(role);   // caller-supplied, not client-supplied
        user.setOrganizationId(organizationId); // the calling admin's own org — never the client's

        User savedUser = userRepository.save(user);
        log.info("User created with role {} for email: {} in organization {}", role, signupRequest.email(), organizationId);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        jwtUtil.verifyRefreshToken(refreshToken);

        UUID userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findUserByUserId(userId).orElseThrow(() ->
                new ResourceNotFoundException("user not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), userId, user.getRole().toString());
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        log.info("Refresh successful for user {}", user.getEmail());
        return new LoginResponse(userId, newAccessToken, newRefreshToken);
    }
}
