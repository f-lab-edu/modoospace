package com.modoospace.alarm.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.common.exception.RedisProcessingException;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@RequiredArgsConstructor
public class EmitterCacheRepository {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private static final Duration EMITTER_CACHE_TTL = Duration.ofHours(1);

  public void save(String loginEmail, SseEmitter sseEmitter) {
    String key = getKey(loginEmail);
    try {
      String value = objectMapper.writeValueAsString(sseEmitter);
      redisTemplate.opsForValue().set(key, value, EMITTER_CACHE_TTL);
    } catch (JsonProcessingException e) {
      throw new RedisProcessingException();
    }
  }

  public Optional<SseEmitter> findByEmail(String loginEmail) {
    String key = getKey(loginEmail);
    String value = redisTemplate.opsForValue().get(key);
    if (value == null) {
      return Optional.empty();
    }

    try {
      SseEmitter sseEmitter = objectMapper.readValue(value, SseEmitter.class);
      return Optional.ofNullable(sseEmitter);
    } catch (JsonProcessingException e) {
      throw new RedisProcessingException();
    }
  }

  public void delete(String loginEmail) {
    String key = getKey(loginEmail);
    redisTemplate.delete(key);
  }

  private String getKey(String loginEmail) {
    return "SSE: " + loginEmail;
  }
}
