package com.example.app.core.command;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.entity.BalanceHistory.BalanceAction;
import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.core.entity.Transaction.TransactionType;
import com.example.app.core.helper.KafkaPublisher;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.core.utils.BalanceAuditService;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.request.TopUpRequest;
import com.example.app.shared.response.TopUpResponse;
import com.example.app.shared.response.embedded.BalanceResponse;
import com.nantaaditya.framework.audit.model.eventbus.IdempotentRecord;
import com.nantaaditya.framework.audit.model.request.AuditRequest;
import com.nantaaditya.framework.audit.service.IdempotentRecordPublisher;
import com.nantaaditya.framework.audit.service.impl.IdempotentCheckExecutor;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
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
        .doOnNext(idempotentRecord -> {
          idempotentRequest.clear();
          idempotentRequest.putAll(idempotentRecord.request());
        })
        .filter(idempotentRecord -> idempotentRecord.idempotent())
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
        .map(tuples -> toTopUpResponse(request, tuples.getT1(), tuples.getT2()))
        .doOnSuccess(response -> idempotentRecordPublisher.publish(toIdempotentRecord(response)));
  }

  private Mono<Tuple2<Member, Balance>> saveTransaction(TopUpRequest request,Member member,
      Balance balance) {

    return Mono.zip(
        saveBalanceAndAudit(request, balance),
        transactionRepository.save(toTransaction(request, member)),
        Tuples::of
    )
        .flatMap(tuples -> balanceHistoryRepository.save(toBalanceHistory(request, member, tuples.getT1(), tuples.getT2()))
            .map(balanceHistory -> Tuples.of(member, tuples.getT1()))
        )
        .as(transactionalOperator::transactional)
        .doOnSuccess(tuple -> kafkaPublisher.publishBalance(tuple.getT2()));
  }

  private Mono<Balance> saveBalanceAndAudit(TopUpRequest request, Balance balance) {
    return balanceAuditService.save(new AuditRequest<>(
        balance.getId(),
        balance.getModifiedBy(),
        balance.getModifiedTime(),
        "topup",
        updateBalance(request, balance))
    );
  }

  private Balance updateBalance(TopUpRequest request, Balance balance) {
    balance.increaseBalance(request.getAmount());
    return balance;
  }

  private Transaction toTransaction(TopUpRequest request, Member member) {
    return Transaction.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .referenceId(request.getReferenceId())
        .amount(request.getAmount())
        .type(TransactionType.TOP_UP)
        .metadata(request.getAdditionalInfo())
        .build();
  }

  private BalanceHistory toBalanceHistory(TopUpRequest request, Member member, Balance balance,
      Transaction transaction) {
    return BalanceHistory.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .amount(request.getAmount())
        .type(balance.getType())
        .action(BalanceAction.CREDIT)
        .transactionId(transaction.getId())
        .build();
  }

  private TopUpResponse toTopUpResponse(TopUpRequest request, Member member, Balance balance) {
    BalanceResponse balanceResponse = ConverterHelper.copy(balance, BalanceResponse::new);
    balanceResponse.setType(balance.getType().toString());

    return TopUpResponse.builder()
        .referenceId(request.getReferenceId())
        .name(member.getName())
        .phoneNumber(member.getPhoneNumber())
        .email(member.getEmail())
        .topUp(balanceResponse)
        .build();
  }

  private IdempotentRecord toIdempotentRecord(TopUpResponse topUpResponse) {
    return new IdempotentRecord(
        IdentifierGenerator.generateId(),
        false,
        "internal",
        "topup",
        idempotentRequest,
        jsonHelper.toJson(topUpResponse),
        System.currentTimeMillis()
    );
  }
}
