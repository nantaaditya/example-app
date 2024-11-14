package com.example.app.core.config;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.entity.Member;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.BalanceAction;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.constant.WorkflowActivityKey;
import com.example.app.shared.constant.WorkflowContextKey;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.event.CashOutEvent;
import com.example.app.shared.request.CashOutRequest;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.workflow.model.dto.WorkflowContext;
import com.nantaaditya.framework.workflow.util.WorkflowProcessorUtil;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuples;

@Slf4j
@Configuration
public class ClearingCashOutListenerConfiguration {

  @Autowired
  private WorkflowProcessorUtil workflowProcessorUtil;

  @Autowired
  private JsonHelper jsonHelper;

  private static final int SINKS_SIZE = 100;

  private static final String WORKFLOW_PROCESSOR_NAME = "cashout";

  @Bean
  public Sinks.Many<CashOutEvent> withdrawEvents() {
    return Sinks.many().replay().all(SINKS_SIZE);
  }

  @EventListener(ApplicationReadyEvent.class)
  public Disposable withdrawListener() {
    return withdrawEvents().asFlux()
        .doOnNext(event -> log.info("event consumer {}", event))
        .delayElements(Duration.ofSeconds(10)) // simulate delay cash out
        .flatMap(event -> workflowProcessorUtil.of(WORKFLOW_PROCESSOR_NAME).execute(createWorkflowContext(event)))
        .subscribe();
  }

  private WorkflowContext createWorkflowContext(CashOutEvent cashOutEvent) {
    String parentId = cashOutEvent.referenceId();
    return new WorkflowContext(
        WORKFLOW_PROCESSOR_NAME,
        parentId,
        cashOutEvent.memberId().concat("_").concat(parentId).concat("_1"),
        WorkflowActivityKey.CLEARING_CASHOUT,
        WorkflowActivityKey.CLEARING_CASHOUT,
        Map.of(WorkflowContextKey.CASHOUT_EVENT, cashOutEvent)
    );
  }
}
