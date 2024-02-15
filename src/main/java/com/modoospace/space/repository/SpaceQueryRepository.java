package com.modoospace.space.repository;

import static com.modoospace.member.domain.QMember.member;
import static com.modoospace.space.domain.QCategory.category;
import static com.modoospace.space.domain.QSpace.space;

import com.modoospace.space.controller.dto.space.SpaceSearchRequest;
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

    private final SpaceIndexQueryRepository spaceIndexQueryRepository;

    public Page<Space> searchSpace(SpaceSearchRequest searchRequest, Pageable pageable) {

        List<Space> content = jpaQueryFactory
            .selectFrom(space)
            .join(space.host, member).fetchJoin()
            .join(space.category, category).fetchJoin()
            .where(
                idInSpaceIndex(searchRequest.getQuery())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Space> countQuery = jpaQueryFactory
            .selectFrom(space)
            .where(
                idInSpaceIndex(searchRequest.getQuery())
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    private BooleanExpression idInSpaceIndex(String query) {
        return query != null ? space.id.in(findByQueryString(query)) : null;
    }

    private List<Long> findByQueryString(String query) {
        return spaceIndexQueryRepository.findByQueryString(query);
    }
}
