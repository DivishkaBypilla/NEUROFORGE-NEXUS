package com.neuroforge.neuroforge.nexus.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank
        String message
) {
}
