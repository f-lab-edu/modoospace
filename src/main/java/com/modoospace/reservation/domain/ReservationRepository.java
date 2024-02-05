package com.modoospace.reservation.domain;

import com.modoospace.member.domain.Member;
import com.modoospace.space.domain.Facility;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  List<Reservation> findByVisitor(Member visitor);

  List<Reservation> findByFacilitySpaceHost(Member host);
}
