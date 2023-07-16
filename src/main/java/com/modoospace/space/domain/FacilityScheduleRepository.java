package com.modoospace.space.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityScheduleRepository extends JpaRepository<FacilitySchedule, Long> {

  List<FacilitySchedule> findByFacilityAndStartDateTimeAfterAndEndDateTimeBeforeOrderByStartDateTime(Facility facility,
      LocalDateTime startDateTime, LocalDateTime endDateTime);
}
