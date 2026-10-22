package com.neuroforge.neuroforge.nexus.entities;

import com.neuroforge.neuroforge.nexus.config.RolePermission;
import com.neuroforge.neuroforge.nexus.entities.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Document(collection = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements UserDetails {

    @Id
    String id;

    UUID userId;

    String name;

    String email;

    Role role;

    // Every user belongs to exactly one organization (tenant) — this is the
    // boundary enforced throughout the app so one company never sees
    // another's projects, tasks, or GitHub integration.
    String organizationId;

    String password;

    // Set when this account is linked to (or was created via) GitHub OAuth.
    // password stays null for accounts that only ever signed in with GitHub.
    String githubId;

    String githubUsername;

    String avatarUrl;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        RolePermission.forRole(role).forEach(p ->
                authorities.add(new SimpleGrantedAuthority(p.name()))
        );

        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
