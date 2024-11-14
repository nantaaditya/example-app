package com.example.app.core.subscriber;

import com.example.app.core.entity.Member;
import com.example.app.core.repository.MemberRepository;
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
public class CreateMemberSubscriber extends SubscriberService {

  protected CreateMemberSubscriber(
      KafkaProperties kafkaProperties, IdempotentEventFilter idempotentEventFilter,
      SchedulerHelper schedulerHelper, CounterMetric kafkaCounterMetric, TimerMetric kafkaTimerMetric,
      KafkaUtil kafkaUtil, MemberRepository memberRepository, JsonHelper jsonHelper) {
    super(new BaseSubscriberServiceBuilder()
        .withKafkaProperties(kafkaProperties)
        .withIdempotentEventFilter(idempotentEventFilter)
        .withSchedulerHelper(schedulerHelper)
        .withKafkaCounterMetric(kafkaCounterMetric)
        .withTimerMetric(kafkaTimerMetric)
        .withKafkaUtil(kafkaUtil)
        .withEventHandler(new MemberEventHandler(memberRepository, jsonHelper))
    );
  }

  @Override
  protected String getTopic() {
    return KafkaTopic.CREATE_MEMBER;
  }

  @Component
  public static class MemberEventHandler implements EventHandler {
    private final MemberRepository memberRepository;
    private final JsonHelper jsonHelper;

    public MemberEventHandler(MemberRepository memberRepository, JsonHelper jsonHelper) {
      this.memberRepository = memberRepository;
      this.jsonHelper = jsonHelper;
    }

    @Override
    public Mono<ReceiverRecord<String, String>> handle(EventType eventType,
        ReceiverRecord<String, String> kafkaRecord) {
      return switch (eventType) {
        case ORIGINAL -> memberRepository.save(Member.from(kafkaRecord, jsonHelper))
            .map(result -> kafkaRecord);
        case DLT -> Mono.just(kafkaRecord)
            .doOnNext(record -> log.warn("#CORE - failed to consume create member event, {}", record.value()));
      };
    }

  }

}
