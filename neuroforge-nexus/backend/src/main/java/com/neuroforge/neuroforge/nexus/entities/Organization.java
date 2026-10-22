package com.neuroforge.neuroforge.nexus.entities;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// A tenant/company using NeuroForge Nexus. Every User and Project belongs to
// exactly one Organization — this is the boundary that keeps one company's
// projects, tasks, and GitHub integration invisible to every other company
// sharing this same database/deployment.
@Document(collection = "organizations")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Organization {

    @Id
    String id;

    String name;

    // URL-safe, unique handle used to identify an org before login (e.g. on
    // the login page, and in the GitHub OAuth redirect round-trip) — e.g.
    // "acme-corp" for "Acme Corp".
    @Indexed(unique = true)
    String slug;

    LocalDateTime createdAt;
}
