package com.modoospace.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.alarm.repository.EmitterMemoryRepository;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@DisplayName("SseEmitterService 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SseEmitterServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private SseEmitterService emitterService;

    @Autowired
    private EmitterMemoryRepository emitterRepository;

    private String testEmail = "test@email.com";

    @AfterEach
    public void after() {
        emitterRepository.delete(testEmail);
    }

    @Test
    public void SseEmitter를_email을_key값으로_메모리에_저장한다() {
        SseEmitter saveEmitter = emitterService.save(testEmail);

        Optional<SseEmitter> optionalEmitter = emitterRepository.find(testEmail);
        assertAll(
                () -> assertThat(optionalEmitter).isPresent(),
                () -> assertThat(optionalEmitter.get()).isEqualTo(saveEmitter)
        );
    }

    @Test
    public void SseEmitter는_email을_key값으로_메모리에서_삭제할수있다() {
        emitterService.save(testEmail);

        emitterService.delete(testEmail);

        Optional<SseEmitter> optionalEmitter = emitterRepository.find(testEmail);
        assertThat(optionalEmitter).isEmpty();
    }

    @Test
    public void SseEmitter는_email을_key값으로_Client에게_데이터를_전송한다() throws IOException {
        // SseEmitter 기능을 테스트하는 것은 무의미하므로 Mock으로 대체한다.
        SseEmitter mockEmitter = mock(SseEmitter.class);
        emitterRepository.save(testEmail, mockEmitter);

        emitterService.sendToClient(testEmail, "testData");

        verify(mockEmitter, times(1)).send(any(SseEventBuilder.class));
    }
}