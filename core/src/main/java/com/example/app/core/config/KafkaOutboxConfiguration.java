package com.example.app.core.config;

import com.example.app.core.entity.Balance;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.nantaaditya.framework.kafka.service.OutboxService;
import com.nantaaditya.framework.kafka.service.impl.OutboxServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaOutboxConfiguration {

  @Bean
  public OutboxService<BalanceRepository, Balance, String, UpdateBalanceEvent> balanceOutboxService(BalanceRepository balanceRepository) {
    return new OutboxServiceImpl<BalanceRepository, Balance, String, UpdateBalanceEvent>() {
      @Override
      protected BalanceRepository getEntityRepository() {
        return balanceRepository;
      }
    };
  }
}
