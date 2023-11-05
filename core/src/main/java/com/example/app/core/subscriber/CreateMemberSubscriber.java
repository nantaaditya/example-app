package com.example.app.core.subscriber;

import com.example.app.core.entity.Member;
import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.filter.IdempotentEventFilter;
import com.nantaaditya.framework.kafka.helper.KafkaUtil;
import com.nantaaditya.framework.kafka.properties.KafkaProperties;
import com.nantaaditya.framework.kafka.service.SubscriberService;
import com.nantaaditya.framework.metric.model.CounterMetric;
import com.nantaaditya.framework.metric.model.TimerMetric;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

@Slf4j
@Component
public class CreateMemberSubscriber extends SubscriberService {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private JsonHelper jsonHelper;

  protected CreateMemberSubscriber(
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
    return KafkaTopic.CREATE_MEMBER;
  }

  @Override
  protected Mono<ReceiverRecord<String, String>> handleEvent(
      ReceiverRecord<String, String> kafkaRecord) {
    return memberRepository.save(Member.from(kafkaRecord, jsonHelper))
        .map(result -> kafkaRecord);
  }

  @Override
  protected Mono<ReceiverRecord<String, String>> handleDltEvent(
      ReceiverRecord<String, String> dltRecord) {
    return Mono.just(dltRecord)
        .doOnNext(record -> log.warn("#CORE - failed to consume create member event, {}", record.value()));
  }
}
