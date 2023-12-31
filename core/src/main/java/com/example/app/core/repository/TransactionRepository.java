package com.example.app.core.repository;

import com.example.app.core.entity.Transaction;
import com.example.app.shared.constant.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends R2dbcRepository<Transaction, String> {

  default Flux<Transaction> findTransactions(String memberId, TransactionType type, Pageable pageable) {
    if (type == null) {
      return findByMemberId(memberId, pageable);
    }
    return findByMemberIdAndType(memberId, type, pageable);
  }
  Flux<Transaction> findByMemberId(String memberId, Pageable pageable);
  Flux<Transaction> findByMemberIdAndType(String memberId, TransactionType type, Pageable pageable);


  default Mono<Long> countTransactions(String memberId, TransactionType type) {
    if (type == null) {
      return countByMemberId(memberId);
    }
    return countByMemberIdAndType(memberId, type);
  }
  Mono<Long> countByMemberId(String memberId);
  Mono<Long> countByMemberIdAndType(String memberId, TransactionType type);
}
