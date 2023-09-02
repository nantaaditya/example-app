package com.example.app.core.config;

import com.example.app.core.entity.IdempotentData;
import com.example.app.core.repository.IdempotentDataRepository;
import com.example.app.core.utils.IdempotentChecker;
import com.nantaaditya.framework.helper.bus.ReactorEventBus;
import com.nantaaditya.framework.helper.idempotent.IdempotentCheckExecutor;
import com.nantaaditya.framework.helper.idempotent.IdempotentCheckStrategy;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.helper.model.IdempotentRecord;
import com.nantaaditya.framework.helper.properties.IdempotentProperties;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;

@Slf4j
@Configuration
public class IdempotentRecordListenerConfiguration {

  private static final int SINKS_SIZE = 100;

  @Autowired
  private ReactorEventBus reactorEventBus;

  @Autowired
  private JsonHelper jsonHelper;

  @Autowired
  private IdempotentDataRepository idempotentDataRepository;

  @Autowired
  private SchedulerHelper schedulerHelper;

  @Bean
  public IdempotentCheckStrategy idempotentChecker(JsonHelper jsonHelper,
      IdempotentProperties idempotentProperties) {
    return new IdempotentChecker(jsonHelper, idempotentProperties, idempotentDataRepository);
  }

  @Bean
  public IdempotentCheckExecutor idempotentCheckExecutor(IdempotentCheckStrategy idempotentChecker) {
    return new IdempotentCheckExecutor(idempotentChecker);
  }

  @Bean
  public Sinks.Many<IdempotentRecord> idempotentRecordEvents() {
    return Sinks.many().replay().all(SINKS_SIZE);
  }

  @EventListener(ApplicationReadyEvent.class)
  public Disposable idempotentListener() {
    Scheduler scheduler = schedulerHelper.of("changelog-schedulers");
    return reactorEventBus.consume(idempotentRecordEvents(), scheduler)
        .map(this::toIdempotentData)
        .doOnNext(idempotentData -> log.info("idempotent data listener {}", idempotentData))
        .flatMap(idempotentDataRepository::save)
        .subscribe();
  }

  private IdempotentData toIdempotentData(IdempotentRecord idempotentRecord) {
    return IdempotentData.builder()
        .id(idempotentRecord.id())
        .client(idempotentRecord.client())
        .category(idempotentRecord.idempotentCategory())
        .createdTime(idempotentRecord.transactionTime())
        .key(jsonHelper.toJson(idempotentRecord.request()))
        .value(idempotentRecord.result())
        .build();
  }

}
