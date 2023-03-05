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
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.request.TopUpRequest;
import com.example.app.shared.response.TopUpResponse;
import com.example.app.shared.response.embedded.BalanceResponse;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
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

  @Override
  public Mono<TopUpResponse> execute(TopUpRequest request) {
    return memberRepository.findById(request.getMemberId())
        .flatMap(member -> balanceRepository.findByTypeAndMemberId(BalanceType.TOPUP_BALANCE, member.getId())
            .map(balance -> Tuples.of(member, balance))
        )
        .flatMap(tuples -> saveTransaction(request, tuples.getT1(), tuples.getT2()))
        .map(tuples -> toTopUpResponse(request, tuples.getT1(), tuples.getT2()));
  }

  private Mono<Tuple2<Member, Balance>> saveTransaction(TopUpRequest request,Member member,
      Balance balance) {

    return Mono.zip(
        balanceRepository.save(updateBalance(request, balance)),
        transactionRepository.save(toTransaction(request, member)),
        Tuples::of
    )
        .flatMap(tuples -> balanceHistoryRepository.save(toBalanceHistory(request, member, tuples.getT1(), tuples.getT2()))
            .map(balanceHistory -> Tuples.of(member, tuples.getT1()))
        )
        .as(transactionalOperator::transactional);
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
}
