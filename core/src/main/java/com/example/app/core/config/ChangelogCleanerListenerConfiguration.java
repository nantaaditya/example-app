package com.example.app.core.config;

import com.nantaaditya.framework.helper.idempotent.IdempotentCheckExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Configuration
@AutoConfigureAfter(ChangelogListenerConfiguration.class)
public class ChangelogCleanerListenerConfiguration {

  @Autowired
  private IdempotentCheckExecutor idempotentCheckExecutor;

  @EventListener(ApplicationReadyEvent.class)
  public Disposable idempotentCleaner() {
    Scheduler scheduler = Schedulers.newBoundedElastic(1, 10, "idempotentCleanerThread");
    return scheduler.schedulePeriodically(() -> idempotentCheckExecutor.removeObsoleteRecord()
        .doOnNext(result -> log.info("running idempotent cleaner scheduler"))
        .subscribe(
            success -> log.info("remove obsolete idempotent record"),
            error -> log.error("failed remove obsolete idempotent record")
        ), 0, 1, TimeUnit.MINUTES);
  }
}
