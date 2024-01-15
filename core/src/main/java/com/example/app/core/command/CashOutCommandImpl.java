package com.example.app.core.command;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.core.utils.BalanceAuditService;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.BalanceAction;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.event.WithdrawEvent;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.example.app.shared.request.CashOutRequest;
import com.example.app.shared.response.CashOutResponse;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.kafka.model.dto.OutboxDTO;
import com.nantaaditya.framework.kafka.service.OutboxService;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashOutCommandImpl implements CashOutCommand {

  private final MemberRepository memberRepository;

  private final BalanceRepository balanceRepository;

  private final BalanceHistoryRepository balanceHistoryRepository;

  private final TransactionRepository transactionRepository;

  private final TransactionalOperator transactionalOperator;

  private final Sinks.Many<WithdrawEvent> withdrawEvents;

  private final BalanceAuditService balanceAuditService;

  private final OutboxService<BalanceRepository, Balance, String, UpdateBalanceEvent> balanceOutboxService;

  private Function<Balance, OutboxDTO<UpdateBalanceEvent>> function = (balance) -> {
    UpdateBalanceEvent updateBalanceEvent = ConverterHelper.copy(balance, UpdateBalanceEvent::new);
    updateBalanceEvent.setType(balance.getType().name());

    return OutboxDTO.create(KafkaTopic.UPDATE_BALANCE, IdentifierGenerator.generateId(), updateBalanceEvent.getId(), updateBalanceEvent);
  };

  @Override
  public Mono<CashOutResponse> execute(CashOutRequest request) {
    return findMember(request)
        .flatMap(member -> saveTransaction(request, member))
        .doOnNext(tuple -> withdrawEvents.tryEmitNext(toEvent(tuple.getT1(), tuple.getT2())))
        .map(tuple -> DtoConverter.toCashOutResponse(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
  }

  private Mono<Member> findMember(CashOutRequest request) {
    return memberRepository.findById(request.getMemberId());
  }

  private Mono<Tuple4<Member, Transaction, Balance, Balance>> saveTransaction(CashOutRequest request,
      Member member) {
    return findBalances(member)
        .flatMap(balances ->
            saveBalancesAndTransaction(request, balances.getT1(), balances.getT2(), member)
              .flatMap(tuple -> saveBalanceHistories(request, tuple.getT1(), tuple.getT2(), tuple.getT3(), member))
              .as(transactionalOperator::transactional)
        );
  }

  private Mono<Tuple2<Balance, Balance>> findBalances(Member member) {
    return Mono.zip(
        balanceRepository.findByTypeAndMemberId(BalanceType.TOPUP_BALANCE, member.getId()),
        balanceRepository.findByTypeAndMemberId(BalanceType.CASHOUT_BALANCE, member.getId()),
        Tuples::of
    );
  }

  private Mono<Tuple3<Balance, Balance, Transaction>> saveBalancesAndTransaction(
      CashOutRequest request, Balance topUpBalance, Balance cashOutBalance, Member member) {
    return Mono.zip(
        saveBalanceAndAudit(request, topUpBalance, '-'),
        saveBalanceAndAudit(request, cashOutBalance, '+'),
        transactionRepository.save(Transaction.from(request, member))
    );
  }

  private Mono<Balance> saveBalanceAndAudit(CashOutRequest request, Balance balance, char operator) {
    return balanceAuditService.save(DtoConverter.toAuditRequest(request.getAmount(), balance,
        '+' == operator ? "topup" : "cashout", operator))
        .flatMap(b -> balanceOutboxService.save(b, function));
  }

  private Mono<Tuple4<Member, Transaction, Balance, Balance>> saveBalanceHistories(
      CashOutRequest request, Balance topUpBalance, Balance cashOutBalance, Transaction transaction,
      Member member) {
    return Mono.zip(
        balanceHistoryRepository.save(BalanceHistory.from(member.getId(), transaction.getId(), request.getAmount(), topUpBalance.getType(), BalanceAction.DEBIT)),
        balanceHistoryRepository.save(BalanceHistory.from(member.getId(), transaction.getId(), request.getAmount(), cashOutBalance.getType(), BalanceAction.CREDIT))
    )
        .map(tuple -> Tuples.of(member, transaction, topUpBalance, cashOutBalance));
  }

  private WithdrawEvent toEvent(Member member, Transaction transaction) {
    return new WithdrawEvent(member.getId(), transaction.getAmount(), transaction.getId());
  }
}
