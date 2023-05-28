package com.example.app.core.utils;

import com.example.app.core.entity.Balance;
import com.example.app.core.repository.BalanceRepository;
import com.nantaaditya.framework.audit.service.AbstractAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BalanceAuditService extends AbstractAuditService<String, Balance, BalanceRepository> {

  @Autowired
  private BalanceRepository balanceRepository;

  @Override
  protected BalanceRepository getRepository() {
    return balanceRepository;
  }
}
