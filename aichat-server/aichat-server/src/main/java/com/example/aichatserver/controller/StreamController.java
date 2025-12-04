package com.example.aichatserver.controller;

import com.example.aichatserver.service.AiService;
import com.example.aichatserver.repo.ConversationRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/stream")
public class StreamController {
    private final AiService aiService;
    private final ConversationRepository convRepo;

    public StreamController(AiService aiService, ConversationRepository convRepo) {
        this.aiService = aiService;
        this.convRepo = convRepo;
    }

    @GetMapping(value = "/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@PathVariable Long conversationId,
                                                @RequestParam String prompt,
                                                @RequestParam(required = false) String model) {
        String subject = String.valueOf(org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()
            .getAttribute("subject", org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST));
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(subject.getBytes());
        long userId = crc.getValue();
        com.example.aichatserver.domain.Conversation conv = convRepo.findById(conversationId).orElse(null);
        if (conv == null || !conv.getUserId().equals(userId)) {
            return Flux.just(ServerSentEvent.builder("forbidden").build(), ServerSentEvent.builder("[DONE]").build());
        }
        return aiService.streamText(conversationId, prompt, model);
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> ping(@RequestParam(defaultValue = "hello") String prompt) {
        String text = "pong " + prompt;
        return Flux.fromIterable(text.chars().mapToObj(c -> String.valueOf((char) c)).toList())
            .map(s -> ServerSentEvent.builder(s).build())
            .concatWith(Flux.just(ServerSentEvent.builder("[DONE]").build()));
    }
}
