package com.example.app.member.configuration;

import com.example.app.member.entity.Balance;
import com.example.app.member.entity.Member;
import com.example.app.member.repository.BalanceRepository;
import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.model.kafka.CreateBalanceEvent;
import com.example.app.shared.model.kafka.CreateMemberEvent;
import com.nantaaditya.framework.kafka.service.OutboxService;
import com.nantaaditya.framework.kafka.service.impl.OutboxServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaOutboxConfiguration {

  @Bean
  public OutboxService<MemberRepository, Member, String, CreateMemberEvent> memberOutboxService(MemberRepository memberRepository) {
    return new OutboxServiceImpl<MemberRepository, Member, String, CreateMemberEvent>() {
      @Override
      public MemberRepository getEntityRepository() {
        return memberRepository;
      }
    };
  }

  @Bean
  public OutboxService<BalanceRepository, Balance, String, CreateBalanceEvent> balanceOutboxService(BalanceRepository balanceRepository) {
    return new OutboxServiceImpl<BalanceRepository, Balance, String, CreateBalanceEvent>() {
      @Override
      public BalanceRepository getEntityRepository() {
        return balanceRepository;
      }
    };
  }

}
