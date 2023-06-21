package com.modoospace.space.domain;

import com.modoospace.member.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {

  List<Space> findByHost(Member host);

  List<Space> findByCategory(Category category);
}
