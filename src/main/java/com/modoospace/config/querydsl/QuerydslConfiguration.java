package com.modoospace.config.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfiguration {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * 프로젝트 내에서 jpaQueryFactory 를 주입받아 사용할 수 있도록 함
   */
  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }

}
