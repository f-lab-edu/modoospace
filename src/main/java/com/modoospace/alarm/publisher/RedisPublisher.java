package com.modoospace.alarm.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.common.exception.MessageParsingError;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisPublisher {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public void publish(String channel, Object data) {
        try {
            String message = objectMapper.writeValueAsString(data);
            redisTemplate.convertAndSend(channel, message);
        } catch (JsonProcessingException e) {
            throw new MessageParsingError(e.getMessage());
        }
    }
}
