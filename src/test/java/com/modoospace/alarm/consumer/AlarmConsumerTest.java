package com.modoospace.alarm.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

class AlarmConsumerTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    AlarmConsumer alarmConsumer;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AlarmRepository alarmRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    public void after() {
        memberRepository.deleteAll();
        alarmRepository.deleteAll();
    }

    @DisplayName("Consumer는 메시지를 소비하여 알람을 저장한다.")
    @Test
    public void handler_ifMember_saveAlarm() throws JsonProcessingException {
        Member member = Member.builder()
                .email("member@email")
                .name("member")
                .role(Role.VISITOR)
                .build();
        memberRepository.save(member);
        AlarmEvent alarmEvent = AlarmEvent.builder()
                .email(member.getEmail())
                .reservationId(1L)
                .facilityName("test facility")
                .alarmType(AlarmType.NEW_RESERVATION)
                .build();
        String message = objectMapper.writeValueAsString(alarmEvent);

        alarmConsumer.handler(message);

        List<Alarm> retAlarms = alarmRepository.findAll();
        assertThat(retAlarms).hasSize(1);
        assertThat(retAlarms).extracting("facilityName")
                .containsExactly("test facility");
    }

    @DisplayName("Consumer는 가입되지 않은 멤버의 메시지를 소비할 경우 Exception을 던진다.")
    @Test
    public void handler_ifNotMember_throwException() throws JsonProcessingException {
        AlarmEvent alarmEvent = AlarmEvent.builder()
                .email("notMember@email.com")
                .reservationId(1L)
                .facilityName("test facility")
                .alarmType(AlarmType.NEW_RESERVATION)
                .build();
        String message = objectMapper.writeValueAsString(alarmEvent);

        assertThatThrownBy(() -> alarmConsumer.handler(message))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class);
    }
}