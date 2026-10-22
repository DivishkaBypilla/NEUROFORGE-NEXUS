package com.neuroforge.neuroforge.nexus.exception;

// Thrown when GitHub's OAuth or REST API can't complete a request — a bad/
// expired code, a misconfigured client secret, a rate limit, or an unlinked/
// invalid repo. Kept distinct from ResourceNotFoundException so the handler
// can return 502 (upstream failure) instead of 404 (nothing to find here).
public class GithubIntegrationException extends RuntimeException {
    public GithubIntegrationException(String message) {
        super(message);
    }
}
