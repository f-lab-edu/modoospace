package com.modoospace.space.repository;

import static com.modoospace.space.domain.QSchedule.schedule;

import com.modoospace.reservation.domain.DateTimeRange;
import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.Schedule;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ScheduleQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 스케줄이 해당 기간을 포함하고있는지 확인합니다.
     */
    public Boolean isIncludingSchedule(Facility facility, DateTimeRange dateTimeRange) {
        LocalDate startDate = dateTimeRange.getStartDate();
        LocalDate endDate = dateTimeRange.getEndDate();

        // 같은 날짜의 시간범위를 체크하는 경우 ex) x월x일 13~18시
        if (startDate.isEqual(endDate)) {
            return isIncludingSchedule(facility, startDate,
                dateTimeRange.getStartTime(), dateTimeRange.getEndTime());
        }

        // 다른 날짜의 시간범위를 체크하는 경우 ex) x월x일 13시~ x월(x+n)일 18시
        // 1. 첫째날 13~24시
        if (isIncludingSchedule(facility, startDate,
            dateTimeRange.getStartTime(), LocalTime.of(23, 59))) {
            // 2. 중간날 0~24시
            int days = Period.between(startDate, endDate).getDays();
            for (int i = 1; i < days; i++) {
                if (!isIncludingSchedule(facility, startDate.plusDays(i),
                    LocalTime.of(0, 0), LocalTime.of(23, 59))) {
                    return false;
                }
            }
            // 3. 마지막날 0~18시
            return isIncludingSchedule(facility, endDate,
                LocalTime.of(0, 0), dateTimeRange.getEndTime());
        }

        return false;
    }

    private boolean isIncludingSchedule(Facility facility, LocalDate date, LocalTime startTime,
        LocalTime endTime) {
        return getIncludingSchedule(facility, date, startTime, endTime) != null;
    }

    private Schedule getIncludingSchedule(Facility facility, LocalDate date, LocalTime startTime,
        LocalTime endTime) {
        return jpaQueryFactory
            .selectFrom(schedule)
            .where(
                facilityEq(facility)
                , dateEq(date)
                , includingTimeRange(startTime, endTime)
            )
            .fetchOne();
    }

    public List<Schedule> find1DaySchedules(Facility facility, LocalDate findDate) {
        return jpaQueryFactory
            .selectFrom(schedule)
            .where(facilityEq(facility)
                , dateEq(findDate))
            .orderBy(schedule.date.asc())
            .orderBy(schedule.timeRange.startTime.asc())
            .fetch();
    }

    public List<Schedule> find1MonthSchedules(Facility facility,
        YearMonth findYearMonth) {
        LocalDate startDate = findYearMonth.atDay(1);
        LocalDate endDate = findYearMonth.atEndOfMonth();

        return jpaQueryFactory
            .selectFrom(schedule)
            .where(facilityEq(facility)
                , dateBetween(startDate, endDate))
            .orderBy(schedule.date.asc())
            .orderBy(schedule.timeRange.startTime.asc())
            .fetch();
    }

    public void delete1MonthSchedules(Facility facility,
        YearMonth findYearMonth) {
        LocalDate startDate = findYearMonth.atDay(1);
        LocalDate endDate = findYearMonth.atEndOfMonth();

        jpaQueryFactory
            .delete(schedule)
            .where(facilityEq(facility)
                , dateBetween(startDate, endDate))
            .execute();
    }

    public void deleteFacilitySchedules(Facility facility) {
        jpaQueryFactory
            .delete(schedule)
            .where(facilityEq(facility))
            .execute();
    }

    private BooleanExpression facilityEq(Facility facility) {
        return facility != null ? schedule.facility.eq(facility) : null;
    }

    private BooleanExpression dateEq(LocalDate date) {
        return date != null ? schedule.date.eq(date) : null;
    }

    private BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null ? schedule.date.between(startDate, endDate)
            : null;
    }

    private BooleanExpression includingTimeRange(LocalTime startTime,
        LocalTime endTime) {
        return startTime != null && endTime != null ?
            (schedule.timeRange.startTime.loe(startTime))
                .and((schedule.timeRange.endTime.goe(endTime))) : null;
    }
}
