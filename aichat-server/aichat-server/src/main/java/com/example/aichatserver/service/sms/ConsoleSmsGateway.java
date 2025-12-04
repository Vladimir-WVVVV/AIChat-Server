package com.example.aichatserver.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleSmsGateway implements SmsGateway {
  private static final Logger log = LoggerFactory.getLogger(ConsoleSmsGateway.class);
  @Override
  public boolean send(String phone, String code) {
    log.info("SMS to {} code {}", phone, code);
    return true;
  }
}

