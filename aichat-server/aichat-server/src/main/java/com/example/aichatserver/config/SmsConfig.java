package com.example.aichatserver.config;

import com.example.aichatserver.service.sms.ConsoleSmsGateway;
import com.example.aichatserver.service.sms.SmsGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfig {
  @Bean
  public SmsGateway smsGateway(ConsoleSmsGateway console) {
    return console;
  }
}
