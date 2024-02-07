package com.modoospace.alarm.controller;

import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.service.AlarmService;
import com.modoospace.config.auth.LoginEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/alarms")
public class AlarmController {

  private final AlarmService alarmService;

  @GetMapping()
  public ResponseEntity<Page<AlarmResponse>> search(@LoginEmail String loginEmail,
      Pageable pageable) {
    Page<AlarmResponse> alarms = alarmService.searchAlarms(loginEmail, pageable);
    return ResponseEntity.ok().body(alarms);
  }

  @DeleteMapping("/{alarmId}")
  public ResponseEntity<Void> delete(@PathVariable Long alarmId,
      @LoginEmail String loginEmail) {
    alarmService.delete(alarmId, loginEmail);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/subscribe")
  public SseEmitter subscribe(@LoginEmail String loginEmail) {
    return alarmService.connectAlarm(loginEmail);
  }
}
