package com.example.app.member.subscriber;

import com.example.app.member.repository.BalanceRepository;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.entity.InboxEvent;
import com.nantaaditya.framework.kafka.properties.InboxProperties;
import com.nantaaditya.framework.kafka.repository.InboxRepositoryImpl;
import com.nantaaditya.framework.kafka.service.AbstractInboxProcessor;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KafkaInboxProcessor extends AbstractInboxProcessor {

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
    if (KafkaTopic.UPDATE_BALANCE.equals(inboxEvent.getTopic())) {
      UpdateBalanceEvent updateBalanceEvent = inboxEvent.getPayload(jsonHelper, new TypeReference<UpdateBalanceEvent>() {});
      return balanceRepository.findById(updateBalanceEvent.getId())
          .filter(Objects::nonNull)
          .filter(existingBalance -> existingBalance.getModifiedTime() < updateBalanceEvent.getModifiedTime())
          .map(existingBalance -> {
            existingBalance.setAmount(updateBalanceEvent.getAmount());
            existingBalance.setModifiedBy(updateBalanceEvent.getModifiedBy());
            existingBalance.setModifiedTime(updateBalanceEvent.getModifiedTime());
            return existingBalance;
          })
          .flatMap(balance -> balanceRepository.save(balance))
          .map(balance -> inboxEvent)
          .doOnNext(event -> log.info("#INBOX - topic {} - {} done processed", inboxEvent.getTopic(), inboxEvent.getId()));
    }

    return Mono.just(inboxEvent)
        .doOnNext(event -> log.info("#INBOX - topic {} not handled", inboxEvent.getTopic()));
  }
}
