package com.modoospace.reservation.domain;

import com.modoospace.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByVisitor(Member visitor);

    List<Reservation> findByFacilitySpaceHost(Member host);
}
