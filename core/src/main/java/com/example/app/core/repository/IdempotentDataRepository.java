package com.example.app.core.repository;

import com.example.app.core.entity.IdempotentData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IdempotentDataRepository extends R2dbcRepository<IdempotentData, String> {
  Mono<IdempotentData> findByKey(String key);

  Mono<Void> deleteByCreatedTimeLessThan(long createdTime);
}
