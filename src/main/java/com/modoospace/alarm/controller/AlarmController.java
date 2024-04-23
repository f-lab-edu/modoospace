package com.modoospace.alarm.controller;

import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.domain.AlarmType;
import com.modoospace.alarm.service.AlarmService;
import com.modoospace.alarm.service.RedisMessageService;
import com.modoospace.config.auth.resolver.LoginMember;
import com.modoospace.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

    private final AlarmService alarmService;
    private final RedisMessageService redisMessageService;


    @GetMapping()
    public ResponseEntity<Page<AlarmResponse>> search(@LoginMember Member loginMember,
            Pageable pageable) {
        Page<AlarmResponse> alarms = alarmService.searchAlarms(loginMember, pageable);
        return ResponseEntity.ok().body(alarms);
    }


    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Void> delete(@PathVariable Long alarmId,
            @LoginMember Member loginMember) {
        alarmService.delete(alarmId, loginMember);
        return ResponseEntity.noContent().build();
    }


    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    public ResponseEntity<SseEmitter> subscribe(@LoginMember Member loginMember) {
        return ResponseEntity.ok(alarmService.connectAlarm(loginMember.getEmail()));
    }

    @PostMapping(value = "/send/{email}")
    public ResponseEntity<Void> send(@PathVariable String email) {
        redisMessageService.publish(email,
                new AlarmResponse(null, null, "테스트시설", AlarmType.NEW_RESERVATION));
        return ResponseEntity.noContent().build();
    }
}
