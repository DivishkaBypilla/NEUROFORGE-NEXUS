package com.neuroforge.neuroforge.nexus.dto.response;

import java.util.UUID;

public record LoginResponse(
        UUID userId,
        String accessToken,
        String refreshToken
) {
}
