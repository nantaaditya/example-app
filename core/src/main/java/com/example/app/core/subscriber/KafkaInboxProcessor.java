package com.example.app.core.subscriber;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.Member;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.model.kafka.CreateBalanceEvent;
import com.example.app.shared.model.kafka.CreateMemberEvent;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.entity.InboxEvent;
import com.nantaaditya.framework.kafka.properties.InboxProperties;
import com.nantaaditya.framework.kafka.repository.InboxRepositoryImpl;
import com.nantaaditya.framework.kafka.service.AbstractInboxProcessor;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KafkaInboxProcessor extends AbstractInboxProcessor {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BalanceRepository balanceRepository;

  @Autowired
  private JsonHelper jsonHelper;

  public KafkaInboxProcessor(
      InboxRepositoryImpl inboxRepository,
      SchedulerHelper schedulerHelper,
      InboxProperties inboxProperties) {
    super(inboxRepository, schedulerHelper, inboxProperties);
  }

  @Override
  protected Mono<InboxEvent> handleProcess(InboxEvent inboxEvent) {
    if (KafkaTopic.CREATE_MEMBER.equals(inboxEvent.getTopic())) {
      return memberRepository.save(Member.from(
          inboxEvent.getPayload(jsonHelper, new TypeReference<CreateMemberEvent>() {}))
      )
          .map(result -> inboxEvent)
          .doOnNext(event -> log.info("#INBOX - topic {} - {} done processed", inboxEvent.getTopic(), inboxEvent.getId()));
    }

    if (KafkaTopic.CREATE_BALANCE.equals(inboxEvent.getTopic())) {
      return balanceRepository.save(Balance.from(
          inboxEvent.getPayload(jsonHelper, new TypeReference<CreateBalanceEvent>() {}))
      )
          .map(result -> inboxEvent)
          .doOnNext(event -> log.info("#INBOX - topic {} - {} done processed", inboxEvent.getTopic(), inboxEvent.getId()));
    }

    return Mono.just(inboxEvent)
        .doOnNext(event -> log.info("#INBOX - topic {} not handled", inboxEvent.getTopic()));
  }
}
