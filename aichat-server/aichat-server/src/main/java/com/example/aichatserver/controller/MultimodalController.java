package com.example.aichatserver.controller;

import com.example.aichatserver.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/multimodal")
public class MultimodalController {
  private final AiService aiService;

  public MultimodalController(AiService aiService) { this.aiService = aiService; }

  @PostMapping(value = "/{conversationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> multimodal(
      @PathVariable Long conversationId,
      @RequestPart("prompt") String prompt,
      @RequestPart(value = "model", required = false) String model,
      @RequestPart(value = "image", required = false) MultipartFile image,
      @RequestPart(value = "imageUrl", required = false) String imageUrl
  ) throws Exception {
    if (image != null) {
      String ct = image.getContentType();
      long sz = image.getSize();
      if (ct == null || !(ct.startsWith("image/jpeg") || ct.startsWith("image/png") || ct.startsWith("image/webp"))) {
        throw new IllegalArgumentException("unsupported_image_type");
      }
      if (sz > 10 * 1024 * 1024) {
        throw new IllegalArgumentException("image_too_large");
      }
    }
    byte[] bytes = image != null ? image.getBytes() : null;
    return aiService.streamImage(conversationId, prompt, model, bytes, imageUrl);
  }
}
