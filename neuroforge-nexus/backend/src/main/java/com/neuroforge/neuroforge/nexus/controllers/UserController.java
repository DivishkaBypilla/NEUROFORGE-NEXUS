package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.dto.request.SignupRequest;
import com.neuroforge.neuroforge.nexus.dto.response.SignupResponse;
import com.neuroforge.neuroforge.nexus.dto.response.UserSummaryResponse;
import com.neuroforge.neuroforge.nexus.entities.User;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import com.neuroforge.neuroforge.nexus.security.CustomUserDetailService;
import com.neuroforge.neuroforge.nexus.service.AuthService;
import com.neuroforge.neuroforge.nexus.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    AuthService authService;

    @GetMapping()
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<List<SignupResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE') or #id == authentication.principal.id")
    public ResponseEntity<SignupResponse> getUserById(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<Void> removeUser(@PathVariable String id){
        userService.removeUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("by-role")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<List<SignupResponse>> getUserByRole(@RequestParam(required = false) String role){
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByRole(role));
    }

    // UserController.java — new endpoint
    @PostMapping("/admin/create")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<SignupResponse> createUser(
            @RequestBody SignupRequest request,
            @RequestParam Role role,
            @AuthenticationPrincipal User principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.createUserWithRole(request, role, principal.getOrganizationId()));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('RBAC_MANAGE')")
    public ResponseEntity<SignupResponse> changeRole(
            @PathVariable String id,
            @RequestParam Role role) {
        return ResponseEntity.ok(userService.updateRole(id, role));
    }

    @GetMapping("/me")
    public ResponseEntity<SignupResponse> getCurrentUser(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(userService.getCurrentUser(principal));
    }

    // Lightweight team directory (id/name/email/role only). No @PreAuthorize — any
    // authenticated user may browse it, e.g. to pick a team lead or a member to add
    // to a project. The result itself is scoped in the service: ADMIN/PROJECT_MANAGER
    // get everyone, everyone else only gets people who share a project with them.
    @GetMapping("/directory")
    public ResponseEntity<List<UserSummaryResponse>> getUserDirectory(@AuthenticationPrincipal User principal) {
        return ResponseEntity.ok(userService.getUserDirectory(principal));
    }


}
