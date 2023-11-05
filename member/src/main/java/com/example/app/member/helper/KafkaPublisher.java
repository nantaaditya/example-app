package com.example.app.member.helper;

import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.kafka.CreateBalanceEvent;
import com.example.app.shared.model.kafka.CreateMemberEvent;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.example.app.shared.response.CreateMemberResponse;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.helper.KafkaHeaderUtil;
import com.nantaaditya.framework.kafka.model.constant.KafkaHeader;
import com.nantaaditya.framework.kafka.service.PublisherService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPublisher {

  private final JsonHelper jsonHelper;

  private final PublisherService publisherService;

  public void publishMember(CreateMemberResponse response) {
    CreateMemberEvent event = ConverterHelper.copy(response.getMember(), CreateMemberEvent::new);
    Set<Header> headers = new HashSet<>();
    headers.add(KafkaHeaderUtil.createHeader("key", event.getId()));

    publisherService.send(
            KafkaTopic.CREATE_MEMBER,
            null,
            jsonHelper.toJson(event),
            headers
        )
        .subscribe();
  }

  public void publishBalance(CreateMemberResponse response) {
    List<CreateBalanceEvent> events = ConverterHelper.copy(response.getBalances(), CreateBalanceEvent::new);
    Set<Header> headers = new HashSet<>();

    for (CreateBalanceEvent event : events) {
      headers.clear();
      headers.add(KafkaHeaderUtil.createHeader(KafkaHeader.KEY, event.getId()));
      headers.add(KafkaHeaderUtil.createHeader(KafkaHeader.UNIQUE_ID, IdentifierGenerator.generateId()));

      event.setMemberId(response.getMember().getId());

      publisherService.send(
              KafkaTopic.CREATE_BALANCE,
              null,
              jsonHelper.toJson(event),
              headers
          )
          .subscribe();
    }
  }
}
