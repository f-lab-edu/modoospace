package com.modoospace.alarm.repository;

import static com.modoospace.alarm.domain.QAlarm.alarm;

import com.modoospace.alarm.domain.Alarm;
import com.modoospace.member.domain.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AlarmQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public Page<Alarm> searchByMember(Member member, Pageable pageable) {

    List<Alarm> content = jpaQueryFactory
        .selectFrom(alarm)
        .where(memberIdEq(member.getId()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Alarm> countQuery = jpaQueryFactory
        .selectFrom(alarm)
        .where(memberIdEq(member.getId()));

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
  }

  private BooleanExpression memberIdEq(Long memberId) {
    return memberId != null ? alarm.memberId.eq(memberId) : null;
  }
}
