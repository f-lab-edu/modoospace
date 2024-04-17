package com.modoospace.alarm.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.common.exception.MessageParsingError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void send(AlarmEvent alarmEvent) {
        try {
            log.info("AlarmEvent produce to x.alarm.work");
            String message = objectMapper.writeValueAsString(alarmEvent);
            rabbitTemplate.convertAndSend("x.alarm.work", "", message);
        } catch (JsonProcessingException e) {
            throw new MessageParsingError();
        }
    }
}
