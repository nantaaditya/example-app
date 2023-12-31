package com.example.app.core.config;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.helper.KafkaPublisher;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.utils.BalanceAuditService;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.BalanceAction;
import com.example.app.shared.constant.BalanceType;
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
public class CashOutListenerConfiguration {

  private static final int SINKS_SIZE = 100;

  @Autowired
  private BalanceRepository balanceRepository;

  @Autowired
  private BalanceHistoryRepository balanceHistoryRepository;

  @Autowired
  private BalanceAuditService balanceAuditService;

  @Autowired
  private KafkaPublisher kafkaPublisher;

  @Bean
  public Sinks.Many<WithdrawEvent> withdrawEvents() {
    return Sinks.many().replay().all(SINKS_SIZE);
  }

  @EventListener(ApplicationReadyEvent.class)
  public Disposable withdrawListener() {
    return withdrawEvents().asFlux()
        .doOnNext(event -> log.info("event consumer {}", event))
        .delayElements(Duration.ofSeconds(10)) // simulate delay cash out
        .flatMap(event -> balanceRepository.findByTypeAndMemberId(BalanceType.CASHOUT_BALANCE, event.memberId())
            .map(balance -> Tuples.of(event, balance))
        )
        .flatMap(tuple -> {
          WithdrawEvent event = tuple.getT1();
          Balance balance = tuple.getT2();
          return Mono.zip(
              saveBalanceAndAudit(tuple.getT1(), tuple.getT2()),
              balanceHistoryRepository.save(
                  BalanceHistory.from(event.memberId(), event.transactionId(), event.transactionAmount(),
                      balance.getType(), BalanceAction.DEBIT
                  )
              )
          );
        })
        .doOnNext(tuple -> {
          log.info("withdraw balance {} & balance history {}", tuple.getT1(), tuple.getT2());
          kafkaPublisher.publishBalance(tuple.getT1());
        })
        .subscribe();
  }

  private Mono<Balance> saveBalanceAndAudit(WithdrawEvent event, Balance balance) {
    return balanceAuditService.save(
        DtoConverter.toAuditRequest(event.transactionAmount(), balance, "cashout",'-')
    );
  }
}
