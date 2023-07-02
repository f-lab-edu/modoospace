package com.modoospace.space.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
  List<Facility> findBySpaceId(Long spaceId);
}
