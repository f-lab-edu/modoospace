package com.modoospace.alarm.service;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.controller.dto.AlarmReadDto;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.domain.EmitterRepository;
import com.modoospace.global.exception.NotFoundEntityException;
import com.modoospace.global.exception.SSEConnectError;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.reservation.domain.ReservationRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
public class AlarmService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
  private static final String ALARM_NAME = "alarm";

  private final MemberRepository memberRepository;
  private final ReservationRepository reservationRepository;
  private final AlarmRepository alarmRepository;
  private final EmitterRepository emitterRepository;

  public List<AlarmReadDto> findAlarmsByMember(String loginEmail) {
    Member loginMember = findMemberByEmail(loginEmail);
    List<Alarm> alarms = alarmRepository.findByMember(loginMember);

    return alarms.stream()
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
      throw new SSEConnectError();
    }

    return sseEmitter;
  }

  @Transactional
  public void saveAndSend(AlarmEvent alarmEvent) {
    Member member = findMemberById(alarmEvent.getMemberId());
    Reservation reservation = findReservationById(alarmEvent.getReservationId());
    Alarm alarm = alarmRepository.save(alarmEvent.toEntity(member, reservation));

    send(alarm.getId(), member.getEmail());
  }

  private void send(Long alarmId, String email) {
    Optional<SseEmitter> optionalSseEmitter = emitterRepository.find(email);
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

  private Member findMemberByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundEntityException("사용자", email));
    return member;
  }

  private Member findMemberById(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new NotFoundEntityException("사용자", memberId));
    return member;
  }

  private Reservation findReservationById(Long reservationId) {
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new NotFoundEntityException("예약", reservationId));
    return reservation;
  }
}
