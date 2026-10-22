package com.neuroforge.neuroforge.nexus.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

// Exactly one of organizationName / organizationSlug must be set:
//   - organizationName: creates a brand new organization, and this user
//     becomes its first member, with the ADMIN role.
//   - organizationSlug: joins an existing organization by its slug, and
//     this user is created as a DEVELOPER.
// Validated in AuthServiceImpl — a record can't express "exactly one of"
// with annotations alone.
public record SignupRequest(
        @NotBlank
        String name,

        @Email
        String email,

        @NotEmpty
        String password,

        String organizationName,

        String organizationSlug
) {
}
