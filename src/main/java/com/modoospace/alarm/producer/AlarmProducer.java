package com.modoospace.alarm.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AlarmProducer {

  private final RabbitTemplate rabbitTemplate;

  public void send(String message) {
    this.rabbitTemplate.convertAndSend("RESERVATION", message);
  }
}
