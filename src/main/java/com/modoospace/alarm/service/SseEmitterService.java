package com.modoospace.alarm.service;

import com.modoospace.alarm.repository.EmitterMemoryRepository;
import com.modoospace.common.exception.SSEConnectError;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
@Slf4j
public class SseEmitterService {

    @Value("${spring.sse.timeout}")
    private Long timeout;

    @Value("${spring.sse.name}")
    private String name;

    private final EmitterMemoryRepository emitterRepository;

    public SseEmitter save(String email) {
        return emitterRepository.save(email, new SseEmitter(timeout));
    }

    public void delete(String email) {
        emitterRepository.delete(email);
    }

    public void sendToClient(String email, Object data) {
        Optional<SseEmitter> optionalSseEmitter = emitterRepository.find(email);
        optionalSseEmitter.ifPresent(sseEmitter -> send(sseEmitter, email, data));
    }

    public void send(SseEmitter emitter, String email, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(email)
                    .name(name)
                    .data(data));
            log.info("SSE Send Event To: {}", email);
        } catch (IOException exception) {
            emitterRepository.delete(email);
            throw new SSEConnectError(exception.getMessage());
        }
    }
}
