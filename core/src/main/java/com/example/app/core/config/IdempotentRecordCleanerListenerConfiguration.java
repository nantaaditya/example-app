package com.example.app.core.config;

import com.nantaaditya.framework.helper.idempotent.IdempotentCheckExecutor;
import com.nantaaditya.framework.reactor.api.SchedulerHelper;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

@Slf4j
@Configuration
@AutoConfigureAfter(IdempotentRecordListenerConfiguration.class)
public class IdempotentRecordCleanerListenerConfiguration {

  @Autowired
  private IdempotentCheckExecutor idempotentCheckExecutor;

  @Autowired
  private SchedulerHelper schedulerHelper;

  @EventListener(ApplicationReadyEvent.class)
  public Disposable idempotentCleaner() {
    Scheduler scheduler = schedulerHelper.of("changelog-schedulers");
    return scheduler.schedulePeriodically(() -> idempotentCheckExecutor.removeObsoleteRecord()
        .doOnNext(result -> log.info("running idempotent cleaner scheduler"))
        .subscribe(
            success -> log.info("remove obsolete idempotent record"),
            error -> log.error("failed remove obsolete idempotent record")
        ), 0, 1, TimeUnit.MINUTES);
  }
}
