package com.example.aichatserver.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.example.aichatserver.service.ZhipuService;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/stream")
public class StreamController {
    private final ZhipuService zhipuService;

    public StreamController(ZhipuService zhipuService) {
        this.zhipuService = zhipuService;
    }

    @GetMapping(value = "/{conversationId}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream(@PathVariable Long conversationId, @RequestParam String prompt) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                boolean ok = zhipuService.stream(prompt, piece -> {
                    try { emitter.send(piece, org.springframework.http.MediaType.TEXT_PLAIN); } catch (Exception ignored) {}
                });
                if (!ok) {
                    String text = "本地回复：" + prompt;
                    for (char c : text.toCharArray()) {
                        emitter.send(String.valueOf(c), org.springframework.http.MediaType.TEXT_PLAIN);
                        Thread.sleep(25);
                    }
                }
                emitter.send("[DONE]", org.springframework.http.MediaType.TEXT_PLAIN);
                emitter.complete();
            } catch (Exception e) {
                try { emitter.send("服务错误", org.springframework.http.MediaType.TEXT_PLAIN); } catch (Exception ignore) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @GetMapping(value = "/ping", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter ping(@RequestParam(defaultValue = "hello") String prompt) {
        SseEmitter emitter = new SseEmitter(0L);
        CompletableFuture.runAsync(() -> {
            try {
                String text = "pong " + prompt;
                for (char c : text.toCharArray()) {
                    emitter.send(String.valueOf(c), org.springframework.http.MediaType.TEXT_PLAIN);
                    Thread.sleep(25);
                }
                emitter.send("[DONE]", org.springframework.http.MediaType.TEXT_PLAIN);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }
}