package com.modoospace.alarm.producer;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AlarmProducerTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    AlarmProducer alarmProducer;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    AlarmRepository alarmRepository;

    @AfterEach
    public void after() {
        memberRepository.deleteAll();
        alarmRepository.deleteAll();
    }

    @DisplayName("Producer가 메세지를 발행하면 Consumer가 해당 메시지를 소비하여 알람을 저장한다.")
    @Test
    public void send_ifMember_saveAlarm() throws InterruptedException {
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
        alarmProducer.send(alarmEvent);

        Thread.sleep(1000);

        List<Alarm> retAlarms = alarmRepository.findAll();
        assertThat(retAlarms).hasSize(1);
        assertThat(retAlarms).extracting("facilityName")
                .containsExactly("test facility");
    }

    @DisplayName("Producer가 가입되지 않은 이메일을 포함한 메세지를 발행하면, Consumer가 해당 메시지를 소비한후 알람을 저장하지 않는다.")
    @Test
    public void send_ifNotMember_notSaveAlarm() throws InterruptedException {
        AlarmEvent alarmEvent = AlarmEvent.builder()
                .email("notMember@email.com")
                .reservationId(1L)
                .facilityName("test facility")
                .alarmType(AlarmType.NEW_RESERVATION)
                .build();
        alarmProducer.send(alarmEvent);

        Thread.sleep(1000);

        List<Alarm> retAlarms = alarmRepository.findAll();
        assertThat(retAlarms).hasSize(0);
    }
}