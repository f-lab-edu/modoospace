package com.modoospace.space.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {

  Optional<Space> findById(Long id);
}
