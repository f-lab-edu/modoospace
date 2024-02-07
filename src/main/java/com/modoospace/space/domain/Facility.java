package com.modoospace.space.domain;

import static javax.persistence.FetchType.LAZY;

import com.modoospace.common.BaseTimeEntity;
import com.modoospace.common.exception.InvalidNumOfUserException;
import com.modoospace.common.exception.LimitNumOfUserException;
import com.modoospace.common.exception.NotOpenedFacilityException;
import com.modoospace.member.domain.Member;
import java.time.YearMonth;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility extends BaseTimeEntity {

    @Id
    @GeneratedValue
    @Column(name = "facility_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean reservationEnable;

    @Column(nullable = false)
    private Integer minUser;

    @Column(nullable = false)
    private Integer maxUser;

    private String description;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @Embedded
    private TimeSettings timeSettings;

    @Embedded
    private WeekdaySettings weekdaySettings;

    @Embedded
    private Schedules schedules;

    @Builder
    public Facility(Long id, String name, Boolean reservationEnable,
        Integer minUser, Integer maxUser, String description,
        Space space, TimeSettings timeSettings, WeekdaySettings weekdaySettings,
        Schedules schedules) {
        this.id = id;
        this.name = name;
        this.reservationEnable = reservationEnable;

        validateUserNum(minUser, maxUser);
        this.minUser = minUser;
        this.maxUser = maxUser;

        this.description = description;
        this.space = space;
        this.timeSettings = timeSettings;
        timeSettings.setFacility(this);
        this.weekdaySettings = weekdaySettings;
        weekdaySettings.setFacility(this);

        this.schedules = schedules;
        if (schedules == null) {
            this.schedules = Schedules
                .create3MonthFacilitySchedules(timeSettings, weekdaySettings, YearMonth.now());
        }
    }

    private void validateUserNum(Integer minUser, Integer maxUser) {
        if (!isValidUserNum(minUser)) {
            throw new InvalidNumOfUserException(minUser);
        }

        if (!isValidUserNum(maxUser)) {
            throw new InvalidNumOfUserException(maxUser);
        }

        if (!isValidMinMaxUserNum(minUser, maxUser)) {
            throw new InvalidNumOfUserException(minUser, maxUser);
        }
    }

    private boolean isValidUserNum(Integer userNum) {
        return userNum > 0;
    }

    private boolean isValidMinMaxUserNum(Integer minUser, Integer maxUser) {
        return minUser <= maxUser;
    }

    public void update(Facility facility) {
        this.name = facility.getName();
        this.reservationEnable = facility.getReservationEnable();
        this.minUser = facility.getMinUser();
        this.maxUser = facility.getMaxUser();
        this.description = facility.getDescription();

        if (facility.shouldUpdateTimeSettings()) {
            this.timeSettings.update(facility.getTimeSettings(), this);
        }

        if (facility.shouldUpdateWeekdaySettings()) {
            this.weekdaySettings.update(facility.getWeekdaySettings(), this);
        }

        if (facility.shouldCreateSchedules()) {
            Schedules schedules = Schedules
                .create3MonthFacilitySchedules(this.timeSettings, this.weekdaySettings,
                    YearMonth.now());
            this.schedules.update(schedules, this);
        }
    }

    private boolean shouldUpdateTimeSettings() {
        return !timeSettings.isEmpty();
    }

    private boolean shouldUpdateWeekdaySettings() {
        return !weekdaySettings.isEmpty();
    }

    public boolean shouldCreateSchedules() {
        return shouldUpdateTimeSettings() || shouldUpdateWeekdaySettings();
    }

    public void create1MonthDefaultSchedules(YearMonth createYearMonth) {
        Schedules schedules = Schedules.create1MonthFacilitySchedules(
            this.timeSettings, this.weekdaySettings, createYearMonth);
        this.schedules.addAll(schedules, this);
    }

    public void addSchedule(Schedule createSchedule) {
        schedules.addSchedule(createSchedule);
    }

    public void updateSchedule(Schedule updateSchedule, Schedule schedule) {
        schedules.updateSchedule(updateSchedule, schedule);
    }

    public void verifyManagementPermission(Member loginMember) {
        space.verifyManagementPermission(loginMember);
    }

    public void verifyReservationEnable() {
        if (!this.reservationEnable) {
            throw new NotOpenedFacilityException();
        }
    }

    public void verityNumOfUser(Integer numOfUser) {
        if (numOfUser < minUser || numOfUser > maxUser) {
            throw new LimitNumOfUserException();
        }
    }
}
