package com.example.aichatserver.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateConversationRequest {
  @NotBlank
  private String title;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
}