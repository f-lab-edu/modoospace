package com.modoospace.alarm.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.service.AlarmService;
import com.modoospace.common.exception.MessageParsingError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmConsumer {

    private final AlarmService alarmService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "q.alarm.work")
    public void handler(String message) {
        log.info("AlarmEvent consume from q.reservation");
        try {
            AlarmEvent alarmEvent = objectMapper.readValue(message, AlarmEvent.class);
            alarmService.saveAndSend(alarmEvent);
        } catch (JsonProcessingException e) {
            throw new MessageParsingError();
        }
    }
}
