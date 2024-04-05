package com.modoospace.alarm.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class AlarmDLQConsumer {

    private static final String RETRY_COUNT_HEADER = "x-retries_count";

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.retry_count}")
    private int retryCount;

    @RabbitListener(queues = "q.alarm.dead")
    public void processFailedMessagesRequeue(Message failedMessage) {
        Integer retriesCnt = (Integer) failedMessage.getMessageProperties().getHeaders()
                .get(RETRY_COUNT_HEADER);
        if (retriesCnt == null) {
            retriesCnt = 0;
        }
        if (retriesCnt >= retryCount) {
            log.info("Discarding message");
            return;
        }
        log.info("Retrying message for the {} time", retriesCnt);
        failedMessage.getMessageProperties().getHeaders().put(RETRY_COUNT_HEADER, ++retriesCnt);
        rabbitTemplate.send("x.alarm.work",
                failedMessage.getMessageProperties().getReceivedRoutingKey(), failedMessage);
    }
}
