package com.modoospace.member.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.common.exception.RedisProcessingException;
import com.modoospace.member.domain.Member;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberCacheRepository {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private static final Duration USER_CACHE_TTL = Duration.ofDays(3);

  public void save(Member member) {
    String key = getKey(member.getEmail());
    try {
      String value = objectMapper.writeValueAsString(member);
      redisTemplate.opsForValue().set(key, value, USER_CACHE_TTL);
      log.info("redis save {} , {}", key, value);
    } catch (JsonProcessingException e) {
      throw new RedisProcessingException();
    }
  }

  public Optional<Member> findByEmail(String email) {
    String key = getKey(email);
    String value = redisTemplate.opsForValue().get(key);
    if (value == null) {
      return Optional.empty();
    }

    try {
      Member member = objectMapper.readValue(value, Member.class);
      log.info("redis find {} , {}", key, value);
      return Optional.ofNullable(member);
    } catch (JsonProcessingException e) {
      throw new RedisProcessingException();
    }
  }

  public String getKey(String email) {
    return "MEMBER: " + email;
  }
}
