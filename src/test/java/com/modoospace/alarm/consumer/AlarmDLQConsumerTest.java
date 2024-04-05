package com.modoospace.alarm.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

class AlarmDLQConsumerTest extends AbstractIntegrationContainerBaseTest {
    private static final String RETRY_COUNT_HEADER = "x-retries_count";

    @Autowired
    private AlarmDLQConsumer alarmDLQConsumer;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Value("${spring.rabbitmq.retry_count}")
    private int retryCount;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AlarmEvent alarmEvent;

    @BeforeEach
    public void setup() {
        Member member = Member.builder()
                .email("member@email")
                .name("member")
                .role(Role.VISITOR)
                .build();
        memberRepository.save(member);
        alarmEvent = AlarmEvent.builder()
                .email(member.getEmail())
                .reservationId(1L)
                .facilityName("test facility")
                .alarmType(AlarmType.NEW_RESERVATION)
                .build();
    }

    @AfterEach
    public void after() {
        memberRepository.deleteAll();
        alarmRepository.deleteAll();
    }

    @DisplayName("RETRY_COUNT_HEADER 값이 없을 경우 0으로 초기화한 후 재처리한다. 그리고 1을 더한다.")
    @Test
    public void processFailedMessagesRequeue_retry_ifHEADER_X_RETRIES_COUNT_null()
            throws JsonProcessingException, InterruptedException {
        String body = objectMapper.writeValueAsString(alarmEvent);
        Message message = new Message(body.getBytes());
        alarmDLQConsumer.processFailedMessagesRequeue(message);

        Thread.sleep(1000);

        List<Alarm> alarms = alarmRepository.findAll();
        assertThat(alarms).hasSize(1);
        assertThat((Integer) message.getMessageProperties().getHeaders()
                .get(RETRY_COUNT_HEADER)).isEqualTo(1);
    }

    @DisplayName("RETRY_COUNT_HEADER 값이 retryCount미만일 경우 재처리한다. 그리고 1을 더한다.")
    @Test
    public void processFailedMessagesRequeue_retry_ifHEADER_X_RETRIES_COUNT_less()
            throws JsonProcessingException, InterruptedException {
        String body = objectMapper.writeValueAsString(alarmEvent);
        Message message = new Message(body.getBytes());
        message.getMessageProperties().getHeaders().put(RETRY_COUNT_HEADER, retryCount - 1);
        alarmDLQConsumer.processFailedMessagesRequeue(message);

        Thread.sleep(1000);

        List<Alarm> alarms = alarmRepository.findAll();
        assertThat(alarms).hasSize(1);
        assertThat((Integer) message.getMessageProperties().getHeaders()
                .get(RETRY_COUNT_HEADER)).isEqualTo(retryCount);
    }

    @DisplayName("RETRY_COUNT_HEADER 값이 3이상일 경우 메세지를 소멸시킨다.")
    @Test
    public void processFailedMessagesRequeue_notRetry_ifHEADER_X_RETRIES_COUNT_over3()
            throws JsonProcessingException, InterruptedException {
        String body = objectMapper.writeValueAsString(alarmEvent);
        Message message = new Message(body.getBytes());
        message.getMessageProperties().getHeaders().put(RETRY_COUNT_HEADER, retryCount);
        alarmDLQConsumer.processFailedMessagesRequeue(message);

        Thread.sleep(1000);

        List<Alarm> alarms = alarmRepository.findAll();
        assertThat(alarms).hasSize(0);
        assertThat((Integer) message.getMessageProperties().getHeaders()
                .get(RETRY_COUNT_HEADER)).isEqualTo(retryCount);
    }
}