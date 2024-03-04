package com.modoospace.alarm.domain;

import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlarmTest {

    private Member host;
    private Member admin;
    private Member visitor;
    private Alarm alarm;

    @BeforeEach
    public void setup() {
        host = Member.builder()
                .id(1L)
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();

        admin = Member.builder()
                .id(1L)
                .email("admin@email")
                .name("admin")
                .role(Role.ADMIN)
                .build();

        visitor = Member.builder()
                .id(1L)
                .email("visitor@email")
                .name("visitor")
                .role(Role.VISITOR)
                .build();

        alarm = Alarm.builder()
                .id(1L)
                .email(host.getEmail())
                .reservationId(1L)
                .facilityName("테스트시설")
                .alarmType(AlarmType.NEW_RESERVATION)
                .build();
    }

    @DisplayName("알람 주인이 아니면 예외를 던진다.")
    @Test
    public void verifyManagementPermission_throwException_ifNotAlarmOwner() {
        assertThatThrownBy(() -> alarm.verifyManagementPermission(visitor))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("알람 주인 또는 Admin일 경우 검증을 통과한다.")
    @Test
    public void verifyManagementPermission_ifAlarmOwnerOrAdmin() {
        alarm.verifyManagementPermission(admin);
        alarm.verifyManagementPermission(host);
    }
}