package com.example.aichatserver.controller;

import com.example.aichatserver.config.AiConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/models")
public class ModelsController {
  private final AiConfig.AiProperties props;
  public ModelsController(AiConfig.AiProperties props) { this.props = props; }

  @GetMapping
  public ResponseEntity<List<Map<String, Object>>> list() {
    java.util.ArrayList<Map<String, Object>> out = new java.util.ArrayList<>();
    if (props.getModelMap() != null) {
      props.getModelMap().forEach((k, v) -> {
        out.add(Map.of("name", k));
      });
    }
    return ResponseEntity.ok(out);
  }
}
