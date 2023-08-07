package com.modoospace.space.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SpaceRepository extends JpaRepository<Space, Long> {

  @Query(value = "select s from Space s "
      + "left join fetch s.host "
      + "left join fetch s.category "
      + "where s.host.id = :hostId"
      , countQuery = "select count(s.id) from Space s "
      + "where s.host.id = :hostId")
  Page<Space> findByHostId(Long hostId, Pageable pageable);

  List<Space> findByCategory(Category category);
}
