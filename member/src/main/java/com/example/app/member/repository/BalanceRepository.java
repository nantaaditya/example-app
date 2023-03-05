package com.example.app.member.repository;

import com.example.app.member.entity.Balance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends R2dbcRepository<Balance, String> {

}
