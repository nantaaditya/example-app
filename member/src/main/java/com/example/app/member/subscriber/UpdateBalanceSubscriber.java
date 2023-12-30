package com.example.app.member.subscriber;

import com.example.app.member.entity.Balance;
import com.example.app.member.repository.BalanceRepository;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.filter.IdempotentEventFilter;
import com.nantaaditya.framework.kafka.helper.KafkaUtil;
import com.nantaaditya.framework.kafka.properties.KafkaProperties;
import com.nantaaditya.framework.kafka.service.SubscriberService;
import com.nantaaditya.framework.metric.model.CounterMetric;
import com.nantaaditya.framework.metric.model.TimerMetric;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Component
public class UpdateBalanceSubscriber extends SubscriberService {

  @Autowired
  private BalanceRepository balanceRepository;

  @Autowired
  private JsonHelper jsonHelper;

  protected UpdateBalanceSubscriber(
      KafkaProperties kafkaProperties,
      IdempotentEventFilter idempotentEventFilter,
      SchedulerHelper schedulerHelper,
      CounterMetric kafkaCounterMetric,
      TimerMetric kafkaTimerMetric,
      KafkaUtil kafkaUtil) {
    super(kafkaProperties, idempotentEventFilter, schedulerHelper, kafkaCounterMetric, kafkaTimerMetric, kafkaUtil);
  }

  @Override
  protected String getTopic() {
    return KafkaTopic.UPDATE_BALANCE;
  }

  @Override
  protected Mono<ReceiverRecord<String, String>> handleEvent(
      ReceiverRecord<String, String> kafkaRecord) {
    return Mono.fromSupplier(() -> Balance.from(kafkaRecord, jsonHelper))
        .flatMap(this::updateBalance)
        .map(result -> kafkaRecord)
        .defaultIfEmpty(kafkaRecord);
  }

  @Override
  protected Mono<ReceiverRecord<String, String>> handleDltEvent(
      ReceiverRecord<String, String> dltRecord) {
    return Mono.just(dltRecord)
        .doOnNext(record -> log.warn("#MEMBER - failed to consume update balance event, {}", record.value()));
  }

  private Mono<Balance> updateBalance(Balance updatedBalance) {
    return balanceRepository.findById(updatedBalance.getId())
        .filter(Objects::nonNull)
        // handle out of order event
        .filter(existingBalance -> existingBalance.getModifiedTime() < updatedBalance.getModifiedTime())
        .map(existingBalance -> {
          existingBalance.setAmount(updatedBalance.getAmount());
          existingBalance.setModifiedBy(updatedBalance.getModifiedBy());
          existingBalance.setModifiedTime(updatedBalance.getModifiedTime());
          return existingBalance;
        })
        .flatMap(balance -> balanceRepository.save(balance));
  }
}
