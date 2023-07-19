package com.modoospace.alarm.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 로컬 캐시로 Emitter 저장 문제 : 여러 개의 서버 인스턴스끼리 공유가 안됨. -> 추후 전체 인스턴스끼리 공유 또는 알림(메세징)이 필요함.
 */
@Repository
public class EmitterRepository {

  private Map<String, SseEmitter> emitterMap = new HashMap<>();

  public SseEmitter save(String loginEmail, SseEmitter sseEmitter) {
    emitterMap.put(loginEmail, sseEmitter);

    return sseEmitter;
  }

  public Optional<SseEmitter> find(String loginEmail) {
    return Optional.ofNullable(emitterMap.get(loginEmail));
  }

  public void delete(String loginEmail) {
    emitterMap.remove(loginEmail);
  }
}
