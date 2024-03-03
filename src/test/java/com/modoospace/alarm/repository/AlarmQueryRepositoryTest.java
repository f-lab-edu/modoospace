package com.modoospace.alarm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.modoospace.JpaTestConfig;
import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.member.domain.Role;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
class AlarmQueryRepositoryTest {

  @Autowired
  private AlarmQueryRepository alarmQueryRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private AlarmRepository alarmRepository;

  private Member hostMember;

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
      alarmRepository.save(testEvent.toEntity());
    }
  }

  @DisplayName("로그인한 멤버의 알람을 조회한다.")
  @Test
  public void searchAlarms(){
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<AlarmResponse> alarms = alarmQueryRepository.searchByMember(hostMember, pageRequest);

    List<AlarmResponse> retContent = alarms.getContent();

    assertThat(retContent).hasSize(10);
    for (AlarmResponse alarm : retContent) {
      System.out.println("alarm = " + alarm.getMessage());
    }
  }
}
