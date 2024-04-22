package com.modoospace.alarm.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.service.SseEmitterService;
import com.modoospace.common.exception.MessageParsingError;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class RedisSubscribeListener implements MessageListener {

    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;

    // 채널 구독 객체
    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
            String email = new String(message.getChannel());
            AlarmResponse alarmResponse = objectMapper.readValue(message.getBody(),
                    AlarmResponse.class);
            log.info("Redis Subscribe Channel: {}", email);
            log.info("Redis Subscribe Message: {}", alarmResponse.getMessage());

            sseEmitterService.sendToClient(email, alarmResponse);
        } catch (IOException e) {
            throw new MessageParsingError(e.getMessage());
        }
    }
}
