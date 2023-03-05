package com.example.app.core.repository;

import com.example.app.core.entity.BalanceHistory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceHistoryRepository extends R2dbcRepository<BalanceHistory, String> {

}
