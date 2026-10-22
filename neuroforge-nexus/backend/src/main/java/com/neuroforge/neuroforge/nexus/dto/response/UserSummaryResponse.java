package com.neuroforge.neuroforge.nexus.dto.response;

import com.neuroforge.neuroforge.nexus.entities.enums.Role;

public record UserSummaryResponse(
        String id,

        String name,

        String email,

        Role role
) {
}