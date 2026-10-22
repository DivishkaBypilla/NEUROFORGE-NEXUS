package com.neuroforge.neuroforge.nexus.dto.response;

public record ChatResponse(
        reactor.core.publisher.Flux<String> message
) {
}
