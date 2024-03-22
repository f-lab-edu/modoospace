package com.modoospace.reservation.controller.dto;

import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.reservation.domain.Reservation;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Schedule;
import com.modoospace.space.domain.Space;
import com.modoospace.space.domain.TimeRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TimeResponseTest {
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate nowDate = now.toLocalDate();
    private final int nowHour = now.getHour();

    private Member visitorMember;

    private Facility facility;

    @BeforeEach
    public void setup() {
        Member hostMember = Member.builder()
                .email("host@email")
                .name("host")
                .role(Role.HOST)
                .build();

        visitorMember = Member.builder()
                .email("visitor@email")
                .name("visitor")
                .role(Role.VISITOR)
                .build();

        Space space = Space.builder()
                .name("스터디")
                .host(hostMember)
                .build();

        facility = Facility.builder()
                .name("스터디룸 1~3인실")
                .reservationEnable(true)
                .minUser(1)
                .maxUser(3)
                .description("1~3인실 입니다.")
                .space(space)
                .build();
    }

    @DisplayName("어제 날짜의 예약가능 여부를 체크하면 available 필드 모두 false를 반환한다.")
    @Test
    public void createTimeResponse_returnAllFalse_ifCheckYesterday() {
        List<Schedule> schedules = List.of(new Schedule(nowDate.minusDays(1), new TimeRange(0, 24)));

        List<TimeResponse> timeResponses = TimeResponse.createTimeResponse(schedules, new ArrayList<>(), nowDate.minusDays(1));

        assertThat(timeResponses).hasSize(24);
        assertThat(timeResponses).extracting("available").containsOnly(false);
    }

    @DisplayName("오늘 날짜의 예약가능 여부를 체크할 경우 현재 시간 이하면 available 필드 모두 false를 반환한다.")
    @Test
    public void createTimeResponse_returnFalse_ifCheckBeforeHour() {
        List<Schedule> schedules = List.of(new Schedule(nowDate, new TimeRange(0, 24)));

        List<TimeResponse> timeResponses = TimeResponse.createTimeResponse(schedules, new ArrayList<>(), nowDate);

        assertThat(timeResponses).hasSize(24);
        timeResponses.stream()
                .filter(timeResponse -> timeResponse.getHour() <= nowHour)
                .forEach(timeResponse -> assertThat(timeResponse.getAvailable()).isFalse());
    }

    @DisplayName("예약가능 여부를 체크하여 예약이 가능하면 available 필드에 true, 불가능하면 false를 반환한다.")
    @Test
    public void createTimeResponse() {
        LocalDate tomorrow = nowDate.plusDays(1);
        List<Schedule> schedules = List.of(new Schedule(tomorrow, new TimeRange(6, 22)));
        List<Reservation> reservations = List.of(createReservation(tomorrow, 6, 10), createReservation(tomorrow, 12, 14));

        List<TimeResponse> timeResponses = TimeResponse.createTimeResponse(schedules, reservations, tomorrow);

        List<TimeResponse> availableResponse = timeResponses.stream()
                .filter(TimeResponse::getAvailable)
                .collect(Collectors.toList());
        assertThat(availableResponse).hasSize(10); // (22-6)-(10-6)-(14-12)
        assertThat(availableResponse).extracting("hour")
                .containsExactly(10, 11, 14, 15, 16, 17, 18, 19, 20, 21);
    }

    private Reservation createReservation(LocalDate tomorrow, int startHour, int endHour) {
        return Reservation.builder()
                .numOfUser(2)
                .dateTimeRange(new DateTimeRange(tomorrow, startHour, tomorrow, endHour))
                .facility(facility)
                .visitor(visitorMember)
                .build();
    }
}