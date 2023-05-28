package com.example.app.core.command;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.BalanceHistory;
import com.example.app.core.entity.BalanceHistory.BalanceAction;
import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.core.entity.Transaction.TransactionType;
import com.example.app.core.repository.BalanceHistoryRepository;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.core.utils.BalanceAuditService;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.event.WithdrawEvent;
import com.example.app.shared.request.CashOutRequest;
import com.example.app.shared.response.CashOutResponse;
import com.example.app.shared.response.embedded.BalanceResponse;
import com.nantaaditya.framework.audit.model.request.AuditRequest;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import java.util.Map;
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

  @Override
  public Mono<CashOutResponse> execute(CashOutRequest request) {
    return findMember(request)
        .flatMap(member -> saveTransaction(request, member))
        .doOnNext(tuple -> withdrawEvents.tryEmitNext(toEvent(tuple.getT1(), tuple.getT2())))
        .map(tuple -> toWithdrawResponse(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()));
  }

  private Mono<Member> findMember(CashOutRequest request) {
    return memberRepository.findById(request.getMemberId());
  }

  private Mono<Tuple4<Member, Transaction, Balance, Balance>> saveTransaction(CashOutRequest request,
      Member member) {
    return findBalances(member)
        .flatMap(balances -> saveBalancesAndTransaction(request, balances.getT1(), balances.getT2(), member)
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
        transactionRepository.save(toTransaction(request, member))
    );
  }

  private Mono<Balance> saveBalanceAndAudit(CashOutRequest request, Balance balance, char operator) {
    return balanceAuditService.save(new AuditRequest<>(
        balance.getId(),
        balance.getModifiedBy(),
        balance.getModifiedTime(),
        '+' == operator ? "topup" : "cashout",
        updateBalance(request, balance, operator)
    ));
  }

  private Mono<Tuple4<Member, Transaction, Balance, Balance>> saveBalanceHistories(
      CashOutRequest request, Balance topUpBalance, Balance cashOutBalance, Transaction transaction,
      Member member) {
    return Mono.zip(
        balanceHistoryRepository.save(toBalanceHistory(request, member, topUpBalance, transaction, BalanceAction.DEBIT)),
        balanceHistoryRepository.save(toBalanceHistory(request, member, cashOutBalance, transaction, BalanceAction.CREDIT))
    )
        .map(tuple -> Tuples.of(member, transaction, topUpBalance, cashOutBalance));
  }

  private Balance updateBalance(CashOutRequest request, Balance balance, char operator) {
    if ('+' == operator) {
      balance.increaseBalance(request.getAmount());
    } else if ('-' == operator) {
      balance.decreaseBalance(request.getAmount());
    }
    return balance;
  }

  private Transaction toTransaction(CashOutRequest request, Member member) {
    return Transaction.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .referenceId(IdentifierGenerator.generateId())
        .amount(request.getAmount())
        .type(TransactionType.CASH_OUT)
        .build();
  }

  private BalanceHistory toBalanceHistory(CashOutRequest request, Member member, Balance balance,
      Transaction transaction, BalanceAction action) {
    return BalanceHistory.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .amount(request.getAmount())
        .type(balance.getType())
        .action(action)
        .transactionId(transaction.getId())
        .build();
  }

  private WithdrawEvent toEvent(Member member, Transaction transaction) {
    return new WithdrawEvent(member.getId(), transaction.getAmount(), transaction.getId());
  }

  private CashOutResponse toWithdrawResponse(Member member, Transaction transaction,
      Balance topUpBalance, Balance withdrawBalance) {
    BalanceResponse topUp = ConverterHelper.copy(topUpBalance, BalanceResponse::new);
    topUp.setType(topUpBalance.getType().toString());
    BalanceResponse withdraw = ConverterHelper.copy(withdrawBalance, BalanceResponse::new);
    withdraw.setType(withdrawBalance.getType().toString());

    return CashOutResponse.builder()
        .phoneNumber(member.getPhoneNumber())
        .referenceId(transaction.getReferenceId())
        .balances(Map.of(
            "topUp", topUp,
            "cashOut", withdraw
        ))
        .build();
  }
}
