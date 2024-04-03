package com.modoospace.alarm.service;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.repository.AlarmQueryRepository;
import com.modoospace.alarm.repository.EmitterLocalCacheRepository;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.common.exception.SSEConnectError;
import com.modoospace.config.redis.aspect.CachePrefixEvict;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.MemberRepository;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class AlarmService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;
    private static final String ALARM_NAME = "sse";

    private final MemberRepository memberRepository;
    private final AlarmRepository alarmRepository;
    private final AlarmQueryRepository alarmQueryRepository;
    private final EmitterLocalCacheRepository emitterRepository;

    public Page<AlarmResponse> searchAlarms(Member loginMember, Pageable pageable) {

        return alarmQueryRepository.searchByMember(loginMember, pageable);
    }

    public SseEmitter connectAlarm(String loginEmail) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(loginEmail, emitter);

        emitter.onCompletion(() -> emitterRepository.delete(loginEmail));
        emitter.onTimeout(() -> emitterRepository.delete(loginEmail));

        sendToClient(emitter, loginEmail, "EventStream Created. [userId=" + loginEmail + "]");
        return emitter;
    }

    @Transactional
    @CachePrefixEvict(cacheNames = "searchAlarms", key = "#alarmEvent.email")
    public void saveAndSend(AlarmEvent alarmEvent) {
        Member member = memberRepository.findByEmail(alarmEvent.getEmail())
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException(
                        "사용자" + alarmEvent.getEmail() + "를 찾을 수 없습니다."));
        Alarm alarm = alarmRepository.save(alarmEvent.toEntity());

        send(member.getEmail(), AlarmResponse.of(alarm));
    }

    public void send(String loginEmail, Object data) {
        Optional<SseEmitter> optionalSseEmitter = emitterRepository.find(loginEmail);
        optionalSseEmitter.ifPresent(sseEmitter -> sendToClient(sseEmitter, loginEmail, data));
    }

    private void sendToClient(SseEmitter emitter, String email, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(email)
                    .name(ALARM_NAME)
                    .data(data));
            log.info("alarm event send SSE: " + email);
        } catch (IOException exception) {
            emitterRepository.delete(email);
            throw new SSEConnectError();
        }
    }

    @Transactional
    @CachePrefixEvict(cacheNames = "searchAlarms", key = "#loginMember.email")
    public void delete(Long alarmId, Member loginMember) {
        Alarm alarm = findAlarmById(alarmId);

        alarm.verifyManagementPermission(loginMember);
        alarmRepository.delete(alarm);
    }

    private Alarm findAlarmById(Long alarmId) {
        return alarmRepository.findById(alarmId).orElseThrow(
                () -> new NotFoundEntityException("알람", alarmId)
        );
    }
}
