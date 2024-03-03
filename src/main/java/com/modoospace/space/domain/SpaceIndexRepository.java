package com.modoospace.space.domain;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SpaceIndexRepository extends ElasticsearchRepository<SpaceIndex, Long> {

}
