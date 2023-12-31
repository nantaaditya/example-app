package com.example.app.core.command;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.core.helper.KafkaPublisher;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.core.utils.BalanceAuditService;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.BalanceAction;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.request.TopUpRequest;
import com.example.app.shared.response.TopUpResponse;
import com.nantaaditya.framework.audit.model.eventbus.IdempotentRecord;
import com.nantaaditya.framework.audit.service.IdempotentRecordPublisher;
import com.nantaaditya.framework.audit.service.impl.IdempotentCheckExecutor;
import com.nantaaditya.framework.helper.json.JsonHelper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class TopUpCommandImpl implements TopUpCommand {

  private final MemberRepository memberRepository;

  private final TransactionRepository transactionRepository;

  private final BalanceRepository balanceRepository;

  private final BalanceHistoryRepository balanceHistoryRepository;

  private final TransactionalOperator transactionalOperator;

  private final IdempotentCheckExecutor idempotentCheckExecutor;

  private final JsonHelper jsonHelper;

  private final IdempotentRecordPublisher idempotentRecordPublisher;

  private final BalanceAuditService balanceAuditService;

  private final KafkaPublisher kafkaPublisher;

  private Map<String, String> idempotentRequest = new HashMap<>();

  @Override
  public Mono<TopUpResponse> execute(TopUpRequest request) {
    return idempotentCheckExecutor.check("internal", request)
        .doOnNext(idempotentRecord -> idempotentRequest.putAll(idempotentRecord.request()))
        .filter(IdempotentRecord::idempotent)
        .map(idempotentRecord -> {
          TopUpResponse response = jsonHelper.fromJson(idempotentRecord.result(), TopUpResponse.class);
          response.setIdempotent(true);
          return response;
        })
        .switchIfEmpty(Mono.defer(() -> executeTopUp(request)));

  }

  private Mono<TopUpResponse> executeTopUp(TopUpRequest request) {
    return memberRepository.findById(request.getMemberId())
        .flatMap(member -> balanceRepository.findByTypeAndMemberId(BalanceType.TOPUP_BALANCE, member.getId())
            .map(balance -> Tuples.of(member, balance))
        )
        .flatMap(tuples -> saveTransaction(request, tuples.getT1(), tuples.getT2()))
        .map(tuples -> DtoConverter.toTopUpResponse(request, tuples.getT1(), tuples.getT2()))
        .doOnSuccess(response -> idempotentRecordPublisher.publish(
            DtoConverter.toTopUpIdempotent(idempotentRequest, response, jsonHelper)))
        .doOnNext(response -> idempotentRequest.clear());
  }

  private Mono<Tuple2<Member, Balance>> saveTransaction(TopUpRequest request,Member member,
      Balance balance) {

    return Mono.zip(
        saveBalanceAndAudit(request, balance),
        transactionRepository.save(Transaction.from(request, member)),
        Tuples::of
    )
        .flatMap(tuples -> balanceHistoryRepository.save(BalanceHistory.from(
              member.getId(), tuples.getT2().getId(), request.getAmount(), tuples.getT1().getType(), BalanceAction.CREDIT)
            )
            .map(balanceHistory -> Tuples.of(member, tuples.getT1()))
        )
        .as(transactionalOperator::transactional)
        .doOnSuccess(tuple -> kafkaPublisher.publishBalance(tuple.getT2()));
  }

  private Mono<Balance> saveBalanceAndAudit(TopUpRequest request, Balance balance) {
    return balanceAuditService.save(DtoConverter.toAuditRequest(request.getAmount(), balance, "topup", '+'));
  }

}
