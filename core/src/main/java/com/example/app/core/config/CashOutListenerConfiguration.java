package com.example.app.core.config;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.utils.BalanceAuditService;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.BalanceAction;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.event.WithdrawEvent;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.kafka.model.dto.OutboxDTO;
import com.nantaaditya.framework.kafka.service.OutboxService;
import java.time.Duration;
import java.util.function.Function;
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
  private OutboxService<BalanceRepository, Balance, String, UpdateBalanceEvent> balanceOutboxService;

  private Function<Balance, OutboxDTO<UpdateBalanceEvent>> function = (balance) -> {
    UpdateBalanceEvent updateBalanceEvent = ConverterHelper.copy(balance, UpdateBalanceEvent::new);
    updateBalanceEvent.setType(balance.getType().name());

    return OutboxDTO.create(KafkaTopic.UPDATE_BALANCE, IdentifierGenerator.generateId(), updateBalanceEvent.getId(), updateBalanceEvent);
  };

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
        .subscribe();
  }

  private Mono<Balance> saveBalanceAndAudit(WithdrawEvent event, Balance balance) {
    return balanceAuditService.save(
        DtoConverter.toAuditRequest(event.transactionAmount(), balance, "cashout",'-')
    )
        .flatMap(b -> balanceOutboxService.save(b, function));
  }
}
