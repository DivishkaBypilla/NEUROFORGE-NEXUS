package com.neuroforge.neuroforge.nexus.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GithubSettingsResponse {

    String clientId;

    boolean hasClientSecret;

    boolean hasApiToken;

    String redirectUri;

    boolean configured;

    LocalDateTime updatedAt;

    String updatedBy;
}
