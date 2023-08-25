package com.modoospace.space.repository;

import static com.modoospace.member.domain.QMember.member;
import static com.modoospace.space.domain.QCategory.category;
import static com.modoospace.space.domain.QSpace.space;

import com.modoospace.space.controller.dto.space.SpaceSearchDto;
import com.modoospace.space.domain.Space;
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
public class SpaceQueryRepository {

  private final JPAQueryFactory jpaQueryFactory;

  public Page<Space> searchSpace(SpaceSearchDto searchDto, Pageable pageable) {

    List<Space> content = jpaQueryFactory
        .selectFrom(space)
        .join(space.host, member).fetchJoin()
        .join(space.category, category).fetchJoin()
        .where(nameContains(searchDto.getName())
            , depthFirstEq(searchDto.getDepthFirst())
            , depthSecondEq(searchDto.getDepthSecond())
            , depthThirdEq(searchDto.getDepthThird())
            , hostIdEq(searchDto.getHostId())
            , categoryIdEq(searchDto.getCategoryId())
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Space> countQuery = jpaQueryFactory
        .selectFrom(space)
        .where(nameContains(searchDto.getName())
            , depthFirstEq(searchDto.getDepthFirst())
            , depthSecondEq(searchDto.getDepthSecond())
            , depthThirdEq(searchDto.getDepthThird())
            , hostIdEq(searchDto.getHostId())
            , categoryIdEq(searchDto.getCategoryId())
        );

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
  }

  private BooleanExpression nameContains(String name) {
    return name != null ? space.name.contains(name) : null;
  }

  private BooleanExpression depthFirstEq(String depthFirst) {
    return depthFirst != null ? space.address.depthFirst.eq(depthFirst) : null;
  }

  private BooleanExpression depthSecondEq(String depthSecond) {
    return depthSecond != null ? space.address.depthSecond.eq(depthSecond) : null;
  }

  private BooleanExpression depthThirdEq(String depthThird) {
    return depthThird != null ? space.address.depthThird.eq(depthThird) : null;
  }

  private BooleanExpression hostIdEq(Long hostId) {
    return hostId != null ? space.host.id.eq(hostId) : null;
  }

  private BooleanExpression categoryIdEq(Long categoryId) {
    return categoryId != null ? space.category.id.eq(categoryId) : null;
  }
}
