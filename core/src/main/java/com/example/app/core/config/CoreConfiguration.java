package com.example.app.core.config;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.entity.BalanceHistory.BalanceAction;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.event.WithdrawEvent;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuples;

@Slf4j
@Configuration
public class CoreConfiguration {

  private static final int SINKS_SIZE = 100;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BalanceRepository balanceRepository;

  @Autowired
  private BalanceHistoryRepository balanceHistoryRepository;

  @Bean
  public Sinks.Many<WithdrawEvent> withdrawEvents() {
    return Sinks.many().replay().all(SINKS_SIZE);
  }

  @EventListener(ApplicationReadyEvent.class)
  public Disposable withdrawListener() {
    return withdrawEvents().asFlux()
        .doOnNext(event -> log.info("event consumer {}", event))
        .delayElements(Duration.ofSeconds(10))
        .flatMap(event -> balanceRepository.findByTypeAndMemberId(BalanceType.CASHOUT_BALANCE, event.memberId())
            .map(balance -> Tuples.of(event, balance))
        )
        .flatMap(tuple -> Mono.zip(
            balanceRepository.save(updateBalance(tuple.getT1(), tuple.getT2())),
            balanceHistoryRepository.save(toBalanceHistory(tuple.getT1(), tuple.getT2()))
        ))
        .doOnNext(tuple ->
          log.info("withdraw balance {} & balance history", tuple.getT1(), tuple.getT2())
        )
        .subscribe();
  }

  private Balance updateBalance(WithdrawEvent event, Balance balance) {
    balance.decreaseBalance(event.transactionAmount());
    return balance;
  }

  private BalanceHistory toBalanceHistory(WithdrawEvent event, Balance balance) {
    return BalanceHistory.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(event.memberId())
        .amount(event.transactionAmount())
        .type(balance.getType())
        .action(BalanceAction.DEBIT)
        .transactionId(event.transactionId())
        .build();
  }
}
