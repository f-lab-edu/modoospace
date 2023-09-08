package com.modoospace.alarm.domain;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@RequiredArgsConstructor
public class EmitterRepository {

  private final RedisTemplate<String, Object> redisTemplate;

  public SseEmitter save(String loginEmail, SseEmitter sseEmitter) {
    String key = getKey(loginEmail);
    redisTemplate.opsForValue().set(key, sseEmitter);

    return sseEmitter;
  }

  public Optional<SseEmitter> find(String loginEmail) {
    String key = getKey(loginEmail);
    SseEmitter sseEmitter = (SseEmitter) redisTemplate.opsForValue().get(key);

    return Optional.ofNullable(sseEmitter);
  }

  public void delete(String loginEmail) {
    String key = getKey(loginEmail);
    redisTemplate.delete(key);
  }

  private String getKey(String loginEmail) {
    return "SSE: " + loginEmail;
  }
}
