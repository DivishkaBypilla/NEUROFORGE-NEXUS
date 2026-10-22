package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// One document per organization (tenant) — id is the owning Organization's
// id, so each company configures and uses its own GitHub OAuth App, editable
// at runtime by that org's admin/project manager from Settings instead of
// being baked into env vars or shared app-wide.
@Document(collection = "integration_settings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntegrationSettings {

    // Equal to the owning Organization's id — one settings document per org.
    @Id
    String id;

    String clientId;

    String clientSecret;

    // Optional server-side personal access token for reading repo data.
    String apiToken;

    // The OAuth App's callback URL, e.g. https://yourapp.com/auth/github/callback.
    String redirectUri;

    LocalDateTime updatedAt;

    // Email of whoever last saved these settings, for a light audit trail.
    String updatedBy;
}
