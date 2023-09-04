package com.modoospace.space.repository;

import static com.modoospace.space.domain.QFacilitySchedule.facilitySchedule;

import com.modoospace.space.domain.Facility;
import com.modoospace.space.domain.FacilitySchedule;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class FacilityScheduleQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  /**
   * 동일한 시설의 스케줄 중 해당기간을 포함하고 있는 스케줄이 있는지 확인합니다. (해당기간이 스케줄의 부분집합인가?)
   *
   * @param facility
   * @param start
   * @param end
   * @return
   */
  public Boolean isIncludingSchedule(Facility facility, LocalDateTime start,
      LocalDateTime end) {

    // 같은 날짜의 시간범위를 체크하는 경우
    if (start.toLocalDate().isEqual(end.toLocalDate())) {
      return getIncludingSchedule(facility, start, end) != null;
    }

    // 다른 날짜의 시간범위를 체크하는 경우
    List<FacilitySchedule> facilitySchedules = getIncludedSchedules(facility, start, end);
    if (facilitySchedules.isEmpty()) {
      return false;
    }

    // 첫번째 스케줄 시작시간, 종료시간(23:59:59) 체크
    FacilitySchedule startDaySchedule = facilitySchedules.get(0);
    LocalDateTime startEndTime = start.toLocalDate().atTime(23, 59, 59);
    if (!startDaySchedule.isIncludingTimeRange(start, startEndTime)) {
      return false;
    }

    // 중간 스케줄 24시간 여부 체크
    if (!IntStream.range(1, facilitySchedules.size() - 1)
        .mapToObj(i -> facilitySchedules.get(i))
        .allMatch(FacilitySchedule::is24TimeRange)) {
      return false;
    }

    // 마지막 스케줄 시작시간(00:00:00), 종료시간 체크
    FacilitySchedule endDaySchedule = facilitySchedules.get(facilitySchedules.size() - 1);
    LocalDateTime endStartTime = end.toLocalDate().atTime(0, 0, 0);
    if (!endDaySchedule.isIncludingTimeRange(endStartTime, end)) {
      return false;
    }

    return true;
  }

  private FacilitySchedule getIncludingSchedule(Facility facility,
      LocalDateTime start, LocalDateTime end) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(facilitySchedule.facility.eq(facility))
        .and(facilitySchedule.startDateTime.before(start)
            .or(facilitySchedule.startDateTime.eq(start)))
        .and(facilitySchedule.endDateTime.after(end)
            .or(facilitySchedule.endDateTime.eq(end)));

    return jpaQueryFactory
        .select(facilitySchedule)
        .from(facilitySchedule)
        .where(builder)
        .fetchOne();
  }

  private List<FacilitySchedule> getIncludedSchedules(Facility facility,
      LocalDateTime start, LocalDateTime end) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(facilitySchedule.facility.eq(facility))
        .and(facilitySchedule.startDateTime.before(end))
        .and(facilitySchedule.endDateTime.after(start));

    return jpaQueryFactory
        .select(facilitySchedule)
        .from(facilitySchedule)
        .where(builder)
        .orderBy(facilitySchedule.startDateTime.asc())
        .fetch();
  }

  public List<FacilitySchedule> find1DaySchedules(Facility facility, LocalDate findDate) {
    LocalDateTime startDateTime = findDate.atTime(0, 0, 0);
    LocalDateTime endDateTime = findDate.atTime(23, 59, 59);

    return getBetweenStartTimeSchedules(facility, startDateTime, endDateTime);
  }

  public List<FacilitySchedule> find1MonthSchedules(Facility facility,
      YearMonth findYearMonth) {
    LocalDateTime startDateTime = findYearMonth.atDay(1).atTime(0, 0, 0);
    LocalDateTime endDateTime = findYearMonth.atEndOfMonth().atTime(23, 59, 59);

    return getBetweenStartTimeSchedules(facility, startDateTime, endDateTime);
  }

  private List<FacilitySchedule> getBetweenStartTimeSchedules(Facility facility,
      LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return jpaQueryFactory
        .selectFrom(facilitySchedule)
        .where(facilitySchedule.facility.id.eq(facility.getId())
            , facilitySchedule.startDateTime.between(startDateTime, endDateTime))
        .orderBy(facilitySchedule.startDateTime.asc())
        .fetch();
  }
}
