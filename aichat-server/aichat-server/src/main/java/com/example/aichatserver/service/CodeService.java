package com.example.aichatserver.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodeService {
  private static class Entry { String code; long expireAt; long lastSendAt; int sendCount; }
  private final Map<String, Entry> store = new ConcurrentHashMap<>();
  private final Random random = new Random();

  public boolean canSend(String phone) {
    Entry e = store.get(phone);
    long now = System.currentTimeMillis();
    if (e == null) return true;
    if (now - e.lastSendAt < 60_000) return false; // 1 min cooldown
    if (e.sendCount >= 5 && now - e.lastSendAt < 3_600_000) return false; // 5/hour
    return true;
  }

  public String genAndStore(String phone) {
    String code = String.format("%04d", random.nextInt(10000));
    Entry e = store.computeIfAbsent(phone, k -> new Entry());
    e.code = code;
    e.expireAt = Instant.now().plusSeconds(300).toEpochMilli(); // 5 minutes
    e.lastSendAt = System.currentTimeMillis();
    e.sendCount++;
    return code;
  }

  public boolean verify(String phone, String code) {
    Entry e = store.get(phone);
    if (e == null) return false;
    if (System.currentTimeMillis() > e.expireAt) return false;
    return code != null && code.equals(e.code);
  }
}

