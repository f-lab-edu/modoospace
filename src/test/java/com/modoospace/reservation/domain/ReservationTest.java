package com.modoospace.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.LimitNumOfUserException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.common.exception.PermissionDeniedException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeRange;
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

    private Member notVisitorMember;

    private Space space;
    private Facility enableFacility;

    private Facility notEnableFacility;

    private DateTimeRange dateTimeRange;

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

        notVisitorMember = Member.builder()
            .id(4L)
            .email("notVisitor@email")
            .name("notVisitor")
            .role(Role.VISITOR)
            .build();

        space = Space.builder()
            .name("test")
            .host(hostMember)
            .build();

        List<TimeSetting> timeSettings = Arrays.asList(TimeSetting.builder()
            .timeRange(new TimeRange(0, 24))
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

        enableFacility = Facility.builder()
            .name("스터디룸 1~3인실")
            .reservationEnable(true)
            .minUser(1)
            .maxUser(3)
            .description("1~3인실 입니다.")
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();

        notEnableFacility = Facility.builder()
            .name("스터디룸 4~6인실")
            .reservationEnable(false)
            .minUser(4)
            .maxUser(6)
            .description("4~6인실 입니다.")
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();

        dateTimeRange = new DateTimeRange(LocalDate.now(), 14, LocalDate.now(), 17);
    }

    @DisplayName("최소인원 미만이면 예약을 생성할 수 없다.")
    @Test
    public void createReservation_throwException_ifSmallMinUser() {
        assertThatThrownBy(() -> Reservation.builder()
            .numOfUser(0)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build()).isInstanceOf(LimitNumOfUserException.class);
    }

    @DisplayName("최대인원 초과이면 예약을 생성할 수 없다.")
    @Test
    public void createReservation_throwException_ifBigMaxUser() {
        assertThatThrownBy(() -> Reservation.builder()
            .numOfUser(4)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build()).isInstanceOf(LimitNumOfUserException.class);
    }

    @DisplayName("시설이 열리지 않았으면 예약을 생성할 수 없다.")
    @Test
    public void createReservation_throwException_ifNotOpenFacility() {
        assertThatThrownBy(() -> Reservation.builder()
            .numOfUser(4)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(notEnableFacility)
            .build())
            .isInstanceOf(NotOpenedFacilityException.class);
    }

    @DisplayName("호스트는 예약을 승인할 수 있다.")
    @Test
    public void approve_ifHost() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        reservation.approveAsHost(hostMember);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @DisplayName("관리자는 예약을 승인할 수 있다.")
    @Test
    public void approve_ifAdmin() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        reservation.approveAsHost(adminMember);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @DisplayName("호스트는 예약을 수정할 수 있다.")
    @Test
    public void update_ifHost() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0));
        DateTimeRange updateDateTimeRange = new DateTimeRange(startDateTime,
            startDateTime.plusHours(5));
        Reservation updateReservation = Reservation.builder()
            .numOfUser(3)
            .dateTimeRange(updateDateTimeRange)
            .status(ReservationStatus.CANCELED)
            .visitor(reservation.getVisitor())
            .facility(reservation.getFacility())
            .build();

        reservation.updateAsHost(updateReservation, hostMember);

        assertAll(
            () -> assertThat(reservation.getNumOfUser()).isEqualTo(3),
            () -> assertThat(reservation.isBetween(LocalDate.now(), 18)),
            () -> assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED)
        );
    }

    @DisplayName("방문자는 예약을 취소할 수 있다.")
    @Test
    public void cancel_ifVisitor() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        reservation.cancelAsVisitor(visitorMember);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @DisplayName("방문자가 아닌자는 예약을 취소할 수 있다.")
    @Test
    public void cancel_throwException_ifVisitor() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        assertThatThrownBy(() -> reservation.cancelAsVisitor(notVisitorMember))
            .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("예약에 접근이 가능한자인지 검증한다.")
    @Test
    public void verifyReservationAccess_ifAdminHostVisitor() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        reservation.verifyReservationAccess(adminMember);
        reservation.verifyReservationAccess(hostMember);
        reservation.verifyReservationAccess(visitorMember);
    }

    @DisplayName("예약에 접근이 불가능한자면 예외를 던진다.")
    @Test
    public void verifyReservationAccess_throwException_ifNotVisitor() {
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        assertThatThrownBy(() -> reservation.verifyReservationAccess(notVisitorMember))
            .isInstanceOf(PermissionDeniedException.class);
    }

    @DisplayName("예약이 해당 시간 사이에 있는지 체크한다.")
    @Test
    public void isBetween_true() {
        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0));
        DateTimeRange customDateTimeRange = new DateTimeRange(startDateTime,
            startDateTime.plusHours(5));
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(customDateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        assertThat(reservation.isBetween(customDateTimeRange.getStartDate(), 15)).isFalse();
        assertThat(reservation.isBetween(customDateTimeRange.getStartDate(), 17)).isTrue();
    }
}
