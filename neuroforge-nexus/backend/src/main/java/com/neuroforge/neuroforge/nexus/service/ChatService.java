package com.neuroforge.neuroforge.nexus.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<String> ask(String message);
}
