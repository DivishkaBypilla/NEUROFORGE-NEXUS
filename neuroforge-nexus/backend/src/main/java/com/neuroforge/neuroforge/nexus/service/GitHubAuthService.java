package com.neuroforge.neuroforge.nexus.service;

import com.neuroforge.neuroforge.nexus.dto.response.LoginResponse;

public interface GitHubAuthService {

    // Exchanges an OAuth "code" (from GitHub's redirect back to the frontend)
    // for the same LoginResponse shape the normal email/password login
    // returns, using the GitHub App configured for the organization
    // identified by organizationSlug, and creating a new account in that
    // organization or linking an existing one (by email) as needed.
    LoginResponse loginWithCode(String code, String organizationSlug);
}
