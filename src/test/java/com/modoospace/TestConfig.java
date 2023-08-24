package com.modoospace;

import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.repository.FacilityQueryRepository;
import com.modoospace.space.repository.FacilityScheduleQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

  @PersistenceContext
  private EntityManager entityManager;

  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }

  @Bean
  public ReservationQueryRepository reservationQueryRepository() {
    return new ReservationQueryRepository(jpaQueryFactory());
  }

  @Bean
  public FacilityQueryRepository facilityQueryRepository() {
    return new FacilityQueryRepository(jpaQueryFactory());
  }

  @Bean
  public FacilityScheduleQueryRepository facilityScheduleQueryRepository() {
    return new FacilityScheduleQueryRepository(jpaQueryFactory());
  }
}
