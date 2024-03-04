package com.modoospace.alarm.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmitterLocalCacheRepository {

    private final Map<String, SseEmitter> sseEmitterMap = new HashMap<>();

    public SseEmitter save(String id, SseEmitter sseEmitter) {
        sseEmitterMap.put(getKey(id), sseEmitter);
        return sseEmitter;
    }

    public Optional<SseEmitter> find(String id) {
        return Optional.ofNullable(sseEmitterMap.get(getKey(id)));
    }

    public void delete(String id) {
        sseEmitterMap.remove(id);
    }

    private String getKey(String id) {
        return "SSE:" + id;
    }
}
