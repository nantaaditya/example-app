package com.example.app.core.repository;

import com.example.app.core.entity.Balance;
import com.example.app.shared.constant.BalanceType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface BalanceRepository extends R2dbcRepository<Balance, String> {
  Mono<Balance> findByTypeAndMemberId(BalanceType type, String memberId);
}
