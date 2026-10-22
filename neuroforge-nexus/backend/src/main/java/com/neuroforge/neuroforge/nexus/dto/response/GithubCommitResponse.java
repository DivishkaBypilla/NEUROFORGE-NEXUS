package com.neuroforge.neuroforge.nexus.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GithubCommitResponse {

    String sha;

    String message;

    String authorName;

    String authorLogin;

    String date;

    String url;
}
