package com.modoospace.alarm.service;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.controller.dto.AlarmReadDto;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.repository.AlarmQueryRepository;
import com.modoospace.alarm.repository.EmitterCacheRepository;
import com.modoospace.common.exception.SSEConnectError;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
public class AlarmService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
  private static final String ALARM_NAME = "alarm";

  private final MemberService memberService;
  private final AlarmRepository alarmRepository;
  private final AlarmQueryRepository alarmQueryRepository;
  private final EmitterCacheRepository emitterRepository;

  public Page<AlarmReadDto> searchAlarms(String loginEmail, Pageable pageable) {
    Member loginMember = memberService.findMemberByEmail(loginEmail);
    Page<Alarm> alarms = alarmQueryRepository.searchByMember(loginMember, pageable);

    return alarms.map(AlarmReadDto::toDto);
  }

  public SseEmitter connectAlarm(String loginEmail) {
    SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(loginEmail, sseEmitter);

    try {
      sseEmitter.send(SseEmitter.event().id("id").name(ALARM_NAME).data("connect completed"));
    } catch (IOException e) {
      throw new SSEConnectError();
    }

    return sseEmitter;
  }

  @Transactional
  public void saveAndSend(AlarmEvent alarmEvent) {
    Member member = memberService.findMemberById(alarmEvent.getMemberId());
    Alarm alarm = alarmRepository.save(alarmEvent.toEntity());

    send(alarm.getId(), member.getEmail());
  }

  private void send(Long alarmId, String email) {
    Optional<SseEmitter> optionalSseEmitter = emitterRepository.findByEmail(email);
    if (optionalSseEmitter.isPresent()) {
      SseEmitter sseEmitter = optionalSseEmitter.get();
      try {
        sseEmitter
            .send(SseEmitter.event().id(alarmId.toString()).name(ALARM_NAME).data("new alarm"));
      } catch (IOException e) {
        emitterRepository.delete(email);
        throw new SSEConnectError();
      }
    }
  }
}
