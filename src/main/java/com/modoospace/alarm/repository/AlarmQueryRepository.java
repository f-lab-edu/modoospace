package com.modoospace.alarm.repository;

import static com.modoospace.alarm.domain.QAlarm.alarm;

import com.modoospace.alarm.controller.dto.AlarmResponse;
import com.modoospace.alarm.domain.Alarm;
import com.modoospace.member.domain.Member;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AlarmQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Cacheable(cacheNames = "searchAlarms", key = "#member.email +':'+ #pageable.pageNumber")
  public Page<AlarmResponse> searchByMember(Member member, Pageable pageable) {

    List<AlarmResponse> content = jpaQueryFactory
        .select(
            Projections.constructor(AlarmResponse.class
                , alarm.id
                , alarm.reservationId
                , alarm.facilityName
                , alarm.alarmType
            )
        )
        .from(alarm)
        .where(emailEq(member.getEmail()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(alarm.createdTime.desc())
        .fetch();

    JPAQuery<Alarm> countQuery = jpaQueryFactory
        .selectFrom(alarm)
        .where(emailEq(member.getEmail()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
  }

  private BooleanExpression emailEq(String email) {
    return email != null ? alarm.email.eq(email) : null;
  }
}
