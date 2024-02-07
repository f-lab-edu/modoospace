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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

        TimeSettings timeSettings = createTimeSettings(new TimeRange(0, 24));
        WeekdaySettings weekDaySettings = createWeekDaySettings(DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        enableFacility = Facility.builder()
            .name("스터디룸 1~3인실")
            .reservationEnable(true)
            .minUser(1)
            .maxUser(3)
            .description("1~3인실 입니다.")
            .timeSettings(timeSettings)
            .weekdaySettings(weekDaySettings)
            .space(space)
            .build();

        notEnableFacility = Facility.builder()
            .name("스터디룸 4~6인실")
            .reservationEnable(false)
            .minUser(4)
            .maxUser(6)
            .description("4~6인실 입니다.")
            .timeSettings(timeSettings)
            .weekdaySettings(weekDaySettings)
            .space(space)
            .build();
    }

    private TimeSettings createTimeSettings(TimeRange... timeRanges) {
        List<TimeSetting> timeSettings = Arrays.stream(timeRanges)
            .map(TimeSetting::new)
            .collect(Collectors.toList());
        return new TimeSettings(timeSettings);
    }

    private WeekdaySettings createWeekDaySettings(DayOfWeek... dayOfWeeks) {
        List<WeekdaySetting> weekdaySettings = Arrays.stream(dayOfWeeks)
            .map(WeekdaySetting::new)
            .collect(Collectors.toList());
        return new WeekdaySettings(weekdaySettings);
    }

    @DisplayName("최소인원 미만이면 예약을 생성할 수 없다.")
    @Test
    public void createReservation_throwException_ifSmallMinUser() {
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        DateTimeRange updateDateTimeRange = new DateTimeRange(
            LocalDate.now(), 17, LocalDate.now(), 22);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 14, LocalDate.now(), 17);
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
        DateTimeRange dateTimeRange = new DateTimeRange(
            LocalDate.now(), 17, LocalDate.now(), 22);
        Reservation reservation = Reservation.builder()
            .numOfUser(2)
            .dateTimeRange(dateTimeRange)
            .visitor(visitorMember)
            .facility(enableFacility)
            .build();

        assertThat(reservation.isBetween(LocalDate.now(), 15)).isFalse();
        assertThat(reservation.isBetween(LocalDate.now(), 17)).isTrue();
    }
}
