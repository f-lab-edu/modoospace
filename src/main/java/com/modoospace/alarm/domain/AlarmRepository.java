package com.modoospace.alarm.domain;

import com.modoospace.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

  List<Alarm> findByMember(Member member);
}
