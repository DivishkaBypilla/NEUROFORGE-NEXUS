package com.neuroforge.neuroforge.nexus.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GithubIssueResponse {

    Integer number;

    String title;

    String state;

    String url;

    String authorLogin;

    String branchRef;

    String createdAt;

    String updatedAt;
}
