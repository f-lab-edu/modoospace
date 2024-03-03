package com.modoospace;

import com.modoospace.alarm.repository.AlarmQueryRepository;
import com.modoospace.reservation.repository.ReservationQueryRepository;
import com.modoospace.space.repository.FacilityQueryRepository;
import com.modoospace.space.repository.ScheduleQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@TestConfiguration
public class JpaTestConfig {

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
    public ScheduleQueryRepository facilityScheduleQueryRepository() {
        return new ScheduleQueryRepository(jpaQueryFactory());
    }

    @Bean
    public AlarmQueryRepository alarmQueryRepository() {
        return new AlarmQueryRepository(jpaQueryFactory());
    }
}
