package com.modoospace.alarm.service;

import com.modoospace.alarm.controller.dto.AlarmEvent;
import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.alarm.domain.AlarmRepository;
import com.modoospace.alarm.repository.AlarmQueryRepository;
import com.modoospace.common.exception.NotFoundEntityException;
import com.modoospace.config.redis.aspect.CachePrefixEvict;
import com.modoospace.member.domain.Member;
import com.modoospace.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
@Slf4j
public class AlarmService {

    private final MemberService memberService;
    private final AlarmRepository alarmRepository;
    private final AlarmQueryRepository alarmQueryRepository;
    private final SseEmitterService sseEmitterService;
    private final RedisMessageService messageService;

    public Page<AlarmResponse> searchAlarms(Member loginMember, Pageable pageable) {

        return alarmQueryRepository.searchByMember(loginMember, pageable);
    }

    public SseEmitter connectAlarm(String loginEmail) {
        SseEmitter sseEmitter = sseEmitterService.save(loginEmail);

        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError((e) -> sseEmitter.complete());
        sseEmitter.onCompletion(() -> sseEmitterService.delete(loginEmail));

        messageService.subscribe(loginEmail); // 채널 구독
        sseEmitterService.send(sseEmitter, loginEmail,
                "EventStream Created. [userId=" + loginEmail + "]"); // dummy 메세지 전송

        return sseEmitter;
    }

    @Transactional
    @CachePrefixEvict(cacheNames = "searchAlarms", key = "#alarmEvent.email")
    public void saveAndSend(AlarmEvent alarmEvent) {
        Member member = memberService.findMemberByEmail(alarmEvent.getEmail());
        Alarm alarm = alarmRepository.save(alarmEvent.toEntity());

        messageService.publish(member.getEmail(), AlarmResponse.of(alarm));
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
