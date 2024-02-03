package com.modoospace.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeSetting;
import com.modoospace.space.domain.TimeSettings;
import com.modoospace.space.domain.WeekdaySetting;
import com.modoospace.space.domain.WeekdaySettings;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReservationTest {

    private Member adminMember;
    private Member hostMember;
    private Member visitorMember;

    private Space space;

    private Facility facilityRoom;

    @BeforeEach
    public void setUp() {

        adminMember = Member.builder()
            .id(1L)
            .email("admin@email")
            .name("admin")
            .role(Role.ADMIN)
            .build();

        hostMember = Member.builder()
            .id(2L)
            .email("host@email")
            .name("host")
            .role(Role.HOST)
            .build();

        visitorMember = Member.builder()
            .id(3L)
            .email("visitor@email")
            .name("visitor")
            .role(Role.VISITOR)
            .build();

        space = Space.builder()
            .name("test")
            .host(hostMember)
            .build();

        List<TimeSetting> timeSettings = Arrays.asList(TimeSetting.builder()
            .startTime(LocalTime.of(0, 0))
            .endTime(LocalTime.of(23, 59, 59))
            .build());

        List<WeekdaySetting> weekdaySettings = Arrays.asList(
            WeekdaySetting.builder()
                .weekday(DayOfWeek.MONDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.TUESDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.WEDNESDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.THURSDAY)
                .build(),
            WeekdaySetting.builder()
                .weekday(DayOfWeek.FRIDAY)
                .build());

        facilityRoom = Facility.builder()
            .name("룸")
            .reservationEnable(true)
            .description("설명")
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();
    }

    @DisplayName("방문자는 룸을 예약할 수 있다.")
    @Test
    public void reservationForRoom() {
        Reservation reservation = Reservation.builder()
            .reservationStart(LocalDateTime.now())
            .reservationEnd(LocalDateTime.now().plusHours(3))
            .visitor(visitorMember)
            .facility(facilityRoom)
            .build();

        assertThat(reservation.getVisitor()).isEqualTo(visitorMember);
    }

    @DisplayName("호스트 및 관리자는 예약을 변경할 수 있다.")
    @Test
    public void updateAsHost() {
        Reservation reservation = Reservation.builder()
            .reservationStart(LocalDateTime.now())
            .reservationEnd(LocalDateTime.now().plusHours(3))
            .visitor(visitorMember)
            .facility(facilityRoom)
            .build();

        assertAll(
            () -> reservation.updateAsHost(reservation, hostMember),
            () -> reservation.updateAsHost(reservation, adminMember)
        );
    }

    @DisplayName("예약 요청자와 방문자가 다를 경우 예외가 발생한다.")
    @Test
    public void verifySameVisitor() {
        Reservation reservation = Reservation.builder()
            .reservationStart(LocalDateTime.now())
            .reservationEnd(LocalDateTime.now().plusHours(3))
            .visitor(hostMember)
            .facility(facilityRoom)
            .build();

        assertThatThrownBy(() -> reservation.verifySameVisitor(visitorMember))
            .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("예약이 해당 시간 사이에 있는지 체크한다.")
    @Test
    public void isReservationBetween() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0, 0));
        Reservation reservation = Reservation.builder()
            .reservationStart(start)
            .reservationEnd(start.plusHours(3))
            .visitor(hostMember)
            .facility(facilityRoom)
            .build();

        assertThat(reservation.isReservationBetween(start.toLocalTime())).isTrue();
        assertThat(reservation.isReservationBetween(start.toLocalTime().minusHours(2))).isFalse();
    }
}
