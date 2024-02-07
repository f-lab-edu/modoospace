package com.modoospace.space.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.modoospace.common.exception.LimitNumOfUserException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.member.domain.Member;
import com.modoospace.member.domain.Role;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FacilityTest {

    private Space space;
    private List<TimeSetting> timeSettings;

    List<WeekdaySetting> weekdaySettings;

    @BeforeEach
    public void before() {
        Member member = Member.builder()
            .id(1L)
            .email("host@email")
            .name("host")
            .role(Role.HOST)
            .build();

        space = Space.builder()
            .name("슈가맨워크")
            .host(member)
            .build();

        timeSettings = Arrays.asList(
            new TimeSetting(new TimeRange(9, 12)), new TimeSetting(new TimeRange(14, 20)));

        weekdaySettings = Arrays.asList(
            new WeekdaySetting(DayOfWeek.WEDNESDAY), new WeekdaySetting(DayOfWeek.THURSDAY),
            new WeekdaySetting(DayOfWeek.FRIDAY), new WeekdaySetting(DayOfWeek.SATURDAY));
    }

    @DisplayName("시설 생성 시 TimeSetting과 WeekSetting에 맞춰 오늘 날짜로 부터 3개월간의 스케줄데이터를 생성한다.")
    @Test
    public void createFacility_24HourOpen_ifNotSelectSetting() {
        Facility facility = Facility.builder()
            .name("4인실")
            .minUser(2)
            .maxUser(4)
            .reservationEnable(true)
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();

        System.out.println(facility.getSchedules());
    }

    @DisplayName("예약이 불가능한 상태일 경우 예외를 던진다.")
    @Test
    public void verifyReservationEnable_throwException_ifNotEnable() {
        Facility facility = Facility.builder()
            .name("4인실")
            .minUser(2)
            .maxUser(4)
            .reservationEnable(false)
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();

        assertAll(
            () -> assertThatThrownBy(facility::verifyReservationEnable).isInstanceOf(
                NotOpenedFacilityException.class)
        );
    }

    @DisplayName("사용인원이 제한된 수보다 적거나 크면 예외를 던진다.")
    @Test
    public void verityNumOfUser_throwException_ifBigOrSmallUserNum() {
        Facility facility = Facility.builder()
            .name("4인실")
            .minUser(2)
            .maxUser(4)
            .reservationEnable(false)
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();

        assertAll(
            () -> assertThatThrownBy(() -> facility.verityNumOfUser(1)).isInstanceOf(
                LimitNumOfUserException.class),
            () -> assertThatThrownBy(() -> facility.verityNumOfUser(5)).isInstanceOf(
                LimitNumOfUserException.class)
        );
    }

    @DisplayName("공간이름 + 시설이름을 반환한다.")
    @Test
    public void getName() {
        Facility facility = Facility.builder()
            .name("4인실")
            .minUser(2)
            .maxUser(4)
            .reservationEnable(false)
            .timeSettings(new TimeSettings(timeSettings))
            .weekdaySettings(new WeekdaySettings(weekdaySettings))
            .space(space)
            .build();

        assertThat(facility.getName()).isEqualTo("슈가맨워크(4인실)");
    }
}
