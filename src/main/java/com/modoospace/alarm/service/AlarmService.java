package com.modoospace.alarm.service;

import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.domain.EmitterRepository;
import com.modoospace.alarm.controller.dto.AlarmReadDto;
import com.modoospace.exception.AlarmConnectError;
import com.modoospace.exception.NotFoundEntityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
public class AlarmService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
  private static final String ALARM_NAME = "alarm";

  private final MemberRepository memberRepository;
  private final AlarmRepository alarmRepository;
  private final EmitterRepository emitterRepository;

  public List<AlarmReadDto> findAlarmsByMember(String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);

    return alarmRepository.findByMember(loginMember).stream()
        .map(AlarmReadDto::toDto)
        .collect(Collectors.toList());
  }

  public SseEmitter connectAlarm(String loginEmail) {
    SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitterRepository.save(loginEmail, sseEmitter);
    sseEmitter.onCompletion(() -> emitterRepository.delete(loginEmail));
    sseEmitter.onTimeout(() -> emitterRepository.delete(loginEmail));

    try {
      sseEmitter.send(SseEmitter.event().id("id").name(ALARM_NAME).data("connect completed"));
    } catch (IOException e) {
      throw new AlarmConnectError();
    }

    return sseEmitter;
  }

  public void send(Long alarmId, String loginEmail) {
    Optional<SseEmitter> optionalSseEmitter = emitterRepository.find(loginEmail);
    if (optionalSseEmitter.isPresent()) {
      SseEmitter sseEmitter = optionalSseEmitter.get();
      try {
        sseEmitter
            .send(SseEmitter.event().id(alarmId.toString()).name(ALARM_NAME).data("new alarm"));
      } catch (IOException e) {
        emitterRepository.delete(loginEmail);
        throw new AlarmConnectError();
      }
    }
  }

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }
}
