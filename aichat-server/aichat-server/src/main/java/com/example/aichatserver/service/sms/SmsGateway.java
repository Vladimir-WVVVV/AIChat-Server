package com.example.aichatserver.service.sms;

public interface SmsGateway {
  boolean send(String phone, String code);
}

