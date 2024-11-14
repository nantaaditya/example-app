package com.example.app.core.command;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.Member;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.constant.WorkflowActivityKey;
import com.example.app.shared.constant.WorkflowContextKey;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.request.CashOutRequest;
import com.example.app.shared.response.CashOutResponse;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.workflow.model.dto.WorkflowContext;
import com.nantaaditya.framework.workflow.util.WorkflowProcessorUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashOutCommandImpl implements CashOutCommand {

  private final MemberRepository memberRepository;

  private final BalanceRepository balanceRepository;

  @Lazy
  private final WorkflowProcessorUtil workflowProcessorUtil;

  private final JsonHelper jsonHelper;

  private static final String WORKFLOW_PROCESSOR_NAME = "cashout";

  @Override
  public Mono<CashOutResponse> execute(CashOutRequest request) {
    return findMember(request)
        .flatMap(member -> findBalances(member)
            .map(balances -> Tuples.of(member, balances.getT1(), balances.getT2()))
        )
        .map(tuple -> Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), createWorkflowContext(tuple.getT1(), request)))
        .doOnNext(tuple -> workflowProcessorUtil.of(WORKFLOW_PROCESSOR_NAME)
            .execute(tuple.getT4())
            .subscribe()
        )
        .map(tuple -> DtoConverter.toCashOutResponse(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4().parentId()));
  }

  private Mono<Member> findMember(CashOutRequest request) {
    return memberRepository.findById(request.getMemberId());
  }

  private Mono<Tuple2<Balance, Balance>> findBalances(Member member) {
    return Mono.zip(
        balanceRepository.findByTypeAndMemberId(BalanceType.TOPUP_BALANCE, member.getId()),
        balanceRepository.findByTypeAndMemberId(BalanceType.CASHOUT_BALANCE, member.getId()),
        Tuples::of
    );
  }

  private WorkflowContext createWorkflowContext(Member member, CashOutRequest cashOutRequest) {
    String parentId = IdentifierGenerator.generateId();
    return new WorkflowContext(
        WORKFLOW_PROCESSOR_NAME,
        parentId,
        member.getId().concat("_").concat(parentId).concat("_0"),
        WorkflowActivityKey.CREATE_CASHOUT,
        WorkflowActivityKey.CREATE_CASHOUT,
        Map.of(WorkflowContextKey.CASHOUT, jsonHelper.toJson(cashOutRequest))
    );
  }
}
