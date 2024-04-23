package com.modoospace.alarm.publisher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.alarm.service.RedisMessageService;
import com.modoospace.alarm.subscriber.RedisSubscribeListener;
import com.modoospace.common.exception.MessageParsingError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.DirtiesContext.MethodMode;


@DisplayName("RedisPublisher 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class RedisPublisherTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private RedisPublisher redisPublisher;

    @Autowired
    private RedisMessageService redisMessageService;

    @MockBean
    private RedisSubscribeListener redisSubscribeListener;

    @BeforeEach
    void setup() {
        doNothing().when(redisSubscribeListener).onMessage(any(Message.class), any(byte[].class));
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void 특정채널에_메시지를_발행하면_채널을_구독하고있는_Listener가_동작한다() {
        redisMessageService.subscribe("test channel");

        AlarmResponse alarmResponse = new AlarmResponse(1L, 1L, "테스트 시설",
                AlarmType.NEW_RESERVATION);
        redisPublisher.publish("test channel", alarmResponse);

        verify(redisSubscribeListener, times(1)).onMessage(any(Message.class), any(byte[].class));
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    public void 특정채널에_메세지발행시_직렬화에_실패하면_Exception을_던진다() {
        redisMessageService.subscribe("test channel");

        assertThatThrownBy(() -> redisPublisher.publish("test channel", new TestDto(1)))
                .isInstanceOf(MessageParsingError.class);
    }

    static class TestDto {

        private int testNum;

        public TestDto(int testNum) {
            this.testNum = testNum;
        }
    }
}