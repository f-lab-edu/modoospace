package com.modoospace.reservation.domain;

import com.modoospace.member.domain.Member;
import com.modoospace.reservation.controller.dto.ReservationReadDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  List<ReservationReadDto> findByVisitor(Member visitor);

}
