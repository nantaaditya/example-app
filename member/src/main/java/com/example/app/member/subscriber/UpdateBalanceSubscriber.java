package com.example.app.member.subscriber;

import com.example.app.member.entity.Balance;
import com.example.app.member.repository.BalanceRepository;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.filter.IdempotentEventFilter;
import com.nantaaditya.framework.kafka.helper.KafkaUtil;
import com.nantaaditya.framework.kafka.model.constant.EventType;
import com.nantaaditya.framework.kafka.properties.KafkaProperties;
import com.nantaaditya.framework.kafka.service.BaseSubscriberServiceBuilder;
import com.nantaaditya.framework.kafka.service.EventHandler;
import com.nantaaditya.framework.kafka.service.SubscriberService;
import com.nantaaditya.framework.metric.model.CounterMetric;
import com.nantaaditya.framework.metric.model.TimerMetric;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Component
public class UpdateBalanceSubscriber extends SubscriberService {

  protected UpdateBalanceSubscriber(
      KafkaProperties kafkaProperties, IdempotentEventFilter idempotentEventFilter,
      SchedulerHelper schedulerHelper, CounterMetric kafkaCounterMetric, TimerMetric kafkaTimerMetric,
      KafkaUtil kafkaUtil, BalanceRepository balanceRepository, JsonHelper jsonHelper
  ) {
    super(new BaseSubscriberServiceBuilder()
        .withKafkaProperties(kafkaProperties)
        .withIdempotentEventFilter(idempotentEventFilter)
        .withSchedulerHelper(schedulerHelper)
        .withKafkaCounterMetric(kafkaCounterMetric)
        .withTimerMetric(kafkaTimerMetric)
        .withKafkaUtil(kafkaUtil)
        .withEventHandler(new BalanceEventHandler(balanceRepository, jsonHelper))
    );
  }

  @Override
  protected String getTopic() {
    return KafkaTopic.UPDATE_BALANCE;
  }

  @Component
  public static class BalanceEventHandler implements EventHandler {
    private final BalanceRepository balanceRepository;
    private final JsonHelper jsonHelper;

    public BalanceEventHandler(BalanceRepository balanceRepository, JsonHelper jsonHelper) {
      this.balanceRepository = balanceRepository;
      this.jsonHelper = jsonHelper;
    }

    @Override
    public Mono<ReceiverRecord<String, String>> handle(EventType eventType,
        ReceiverRecord<String, String> kafkaRecord) {
      return switch (eventType) {
        case ORIGINAL -> Mono.fromSupplier(() -> Balance.from(kafkaRecord, jsonHelper))
            .flatMap(this::updateBalance)
            .map(result -> kafkaRecord)
            .defaultIfEmpty(kafkaRecord);
        case DLT -> Mono.just(kafkaRecord)
            .doOnNext(record -> log.warn("#MEMBER - failed to consume update balance event, {}", record.value()));
      };
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

}
