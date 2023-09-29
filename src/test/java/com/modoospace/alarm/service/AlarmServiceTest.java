package com.modoospace.alarm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AlarmServiceTest {

  @Autowired
  private AlarmService alarmService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private AlarmRepository alarmRepository;

  @Autowired
  private StringRedisTemplate redisTemplate;

  private Member hostMember;

  private Alarm alarm;

  @BeforeEach
  public void setUp() {
    hostMember = Member.builder()
        .email("host@email")
        .name("host")
        .role(Role.HOST)
        .build();

    memberRepository.save(hostMember);

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
    redisTemplate.getConnectionFactory().getConnection().flushAll();
  }

  @DisplayName("알람을 검색하면, searchAlarms::이메일:페이지넘버를 키값으로 결과가 캐싱된다.")
  @Test
  public void searchAlarms_redisSave() {
    PageRequest pageRequest = PageRequest.of(0, 10);
    alarmService.searchAlarms("host@email", pageRequest);

    String pattern = "searchAlarms::" + hostMember.getEmail() + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    assertThat(keys).hasSize(1);
    System.out.println("keys = " + keys);
  }

  @DisplayName("알람을 새로 save하면 redis에 저장되어있던 해당 멤버의 알람 페이징 데이터가 전부 삭제된다.")
  @Test
  public void saveAndSend_EmptyRedisKeys() {
    PageRequest pageRequest = PageRequest.of(0, 10);
    alarmService.searchAlarms("host@email", pageRequest);
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
    alarmService.searchAlarms("host@email", pageRequest);

    alarmService.delete(alarm.getId(), "host@email");

    String pattern = "searchAlarms::" + hostMember.getEmail() + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    assertThat(keys).isEmpty();
  }
}
