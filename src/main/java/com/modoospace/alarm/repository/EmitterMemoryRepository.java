package com.modoospace.alarm.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
@RequiredArgsConstructor
public class EmitterMemoryRepository {

    // thread-safe한 자료구조
    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public SseEmitter save(String id, SseEmitter sseEmitter) {
        sseEmitterMap.put(getKey(id), sseEmitter);
        return sseEmitter;
    }

    public Optional<SseEmitter> find(String id) {
        return Optional.ofNullable(sseEmitterMap.get(getKey(id)));
    }

    public void delete(String id) {
        sseEmitterMap.remove(getKey(id));
    }

    private String getKey(String id) {
        return "SSE:" + id;
    }
}
