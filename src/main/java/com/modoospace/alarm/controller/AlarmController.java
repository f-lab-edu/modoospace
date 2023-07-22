package com.modoospace.alarm.controller;

import com.modoospace.alarm.service.AlarmService;
import com.modoospace.alarm.controller.dto.AlarmReadDto;
import com.modoospace.config.auth.LoginEmail;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

  private final AlarmService alarmService;

  @GetMapping()
  public ResponseEntity<List<AlarmReadDto>> findAlarms(@LoginEmail String loginEmail) {
    List<AlarmReadDto> alarms = alarmService.findAlarmsByMember(loginEmail);
    return ResponseEntity.ok().body(alarms);
  }

  @GetMapping("/subscribe")
  public SseEmitter subscribe(@LoginEmail String loginEmail) {
    return alarmService.connectAlarm(loginEmail);
  }
}
