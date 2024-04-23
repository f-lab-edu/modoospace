package com.modoospace.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.AbstractIntegrationContainerBaseTest;
import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.alarm.repository.EmitterMemoryRepository;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Transactional
class AlarmServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private EmitterMemoryRepository emitterRepository;

    private Member hostMember;

    private Alarm alarm;

    @BeforeEach
    public void setUp() {
        hostMember = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();

        hostMember = memberRepository.save(hostMember);

        for (int i = 0; i < 10; i++) {
            AlarmEvent testEvent = AlarmEvent.builder()
                    .email(hostMember.getEmail())
                    .reservationId((long) i)
                    .facilityName("테스트 시설")
                    .alarmType(AlarmType.NEW_RESERVATION)
                    .build();
            alarm = alarmRepository.save(testEvent.toEntity());
        }
    }

    @AfterEach
    public void after() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    @DisplayName("알람을 검색하면, searchAlarms::이메일:페이지넘버를 키값으로 결과가 캐싱된다.")
    @Test
    public void searchAlarms_redisSave() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        alarmService.searchAlarms(hostMember, pageRequest);

        String pattern = "searchAlarms::" + hostMember.getEmail() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        assertThat(keys).hasSize(1);
        assertThat(keys).contains("searchAlarms::host@email:0");
    }

    @DisplayName("로그인한 Email을 key값으로 SseEmitter를 저장한다.")
    @Test
    public void connectAlarm_saveSseEmitterByEmail() {
        SseEmitter sseEmitter = alarmService.connectAlarm(hostMember.getEmail());

        SseEmitter retSseEmitter = emitterRepository.find(hostMember.getEmail()).get();

        assertThat(retSseEmitter).isEqualTo(sseEmitter);
    }

    @DisplayName("알람을 새로 save하면 redis에 저장되어있던 해당 멤버의 알람 페이징 데이터가 전부 삭제된다.")
    @Test
    public void saveAndSend_EmptyRedisKeys() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        alarmService.searchAlarms(hostMember, pageRequest);
        AlarmEvent testEvent = AlarmEvent.builder()
                .email(hostMember.getEmail())
                .reservationId(1L)
                .facilityName("테스트 시설")
                .alarmType(AlarmType.NEW_RESERVATION)
                .build();

        alarmService.saveAndSend(testEvent);

        String pattern = "searchAlarms::" + hostMember.getEmail() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        assertThat(keys).isEmpty();
    }

    @DisplayName("알람을 삭제하면 redis에 저장되어있던 해당 멤버의 알람 페이징 데이터가 전부 삭제된다.")
    @Test
    public void deleteAlarm_EmptyRedisKeys() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        alarmService.searchAlarms(hostMember, pageRequest);

        alarmService.delete(alarm.getId(), hostMember);

        String pattern = "searchAlarms::" + hostMember.getEmail() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        assertThat(keys).isEmpty();
    }
}
