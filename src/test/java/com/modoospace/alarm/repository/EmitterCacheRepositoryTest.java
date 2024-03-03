package com.modoospace.alarm.repository;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EmitterCacheRepositoryTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private EmitterCacheRepository emitterCacheRepository;

    private SseEmitter sseEmitter;

    @BeforeEach
    public void setup() {
        sseEmitter = new SseEmitter(60L * 1000 * 60);
    }

    @DisplayName("SseEmitter를 \'SSE:이메일\' 을 키값으로 저장한다.")
    @Test
    public void save() {
        emitterCacheRepository.save("test@email.com", sseEmitter);

        Optional<SseEmitter> retSseEmitter = emitterCacheRepository.findByEmail("test@email.com");
        assertThat(retSseEmitter).isNotEmpty();
    }

    @DisplayName("SseEmitter를 \'SSE:이메일\' 을 키값으로 삭제한다.")
    @Test
    public void delete() {
        emitterCacheRepository.save("test@email.com", sseEmitter);

        emitterCacheRepository.delete("test@email.com");

        Optional<SseEmitter> retSseEmitter = emitterCacheRepository.findByEmail("test@email.com");
        assertThat(retSseEmitter).isEmpty();
    }
}