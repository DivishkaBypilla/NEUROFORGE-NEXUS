package com.neuroforge.neuroforge.nexus.controllers;

import com.neuroforge.neuroforge.nexus.dto.request.ChatRequest;
import com.neuroforge.neuroforge.nexus.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // 1. Return type MUST be Flux<String>, NOT ChatResponse
    public Flux<String> chat(
            @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        System.out.println("Chat request from user: " + username);

        // 2. Return the Flux directly from the service
        return chatService.ask(request.message());
    }
}