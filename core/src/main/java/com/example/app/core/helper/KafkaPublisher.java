package com.example.app.core.helper;

import com.example.app.core.entity.Balance;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.kafka.helper.KafkaHeaderUtil;
import com.nantaaditya.framework.kafka.model.constant.KafkaHeader;
import com.nantaaditya.framework.kafka.service.PublisherService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.header.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaPublisher {

  private final JsonHelper jsonHelper;

  private final PublisherService publisherService;

  public void publishBalance(Balance balance) {
    UpdateBalanceEvent event = ConverterHelper.copy(balance, UpdateBalanceEvent::new);
    event.setType(balance.getType().name());

    Set<Header> headers = new HashSet<>();
    headers.add(KafkaHeaderUtil.createHeader(KafkaHeader.KEY, event.getId()));
    headers.add(KafkaHeaderUtil.createHeader(KafkaHeader.UNIQUE_ID, IdentifierGenerator.generateId()));

    publisherService.send(
        KafkaTopic.UPDATE_BALANCE,
        null,
        jsonHelper.toJson(event),
        headers
    )
        .subscribe();
  }
}
