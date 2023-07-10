package com.modoospace.space.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FacilityScheduleRepository extends JpaRepository<FacilitySchedule, Long> {

    List<FacilitySchedule> findByFacilityAndStartDateTimeContainingAndEndDateTimeContaining(Facility facility, LocalDate startDate, LocalDate endDate);
}
