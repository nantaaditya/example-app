package com.example.app.core.repository;

import com.example.app.core.entity.Member;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MemberRepository extends R2dbcRepository<Member, String> {
  Mono<Boolean> existsByEmailOrPhoneNumber(String email, String phoneNumber);
}
