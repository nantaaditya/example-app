package com.example.app.core.subscriber;

import com.example.app.core.entity.Balance;
import com.example.app.core.repository.BalanceRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Component
public class CreateBalanceSubscriber extends SubscriberService {

  protected CreateBalanceSubscriber(
      KafkaProperties kafkaProperties, IdempotentEventFilter idempotentEventFilter,
      SchedulerHelper schedulerHelper, CounterMetric kafkaCounterMetric, TimerMetric kafkaTimerMetric,
      KafkaUtil kafkaUtil, BalanceRepository balanceRepository, JsonHelper jsonHelper) {
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
    return KafkaTopic.CREATE_BALANCE;
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
        case ORIGINAL -> balanceRepository.save(Balance.from(kafkaRecord, jsonHelper))
            .map(result -> kafkaRecord);
        case DLT -> Mono.just(kafkaRecord)
            .doOnNext(record -> log.warn("#CORE  - failed to consume create member event, {}", record.value()));
      };
    }
  }

}
