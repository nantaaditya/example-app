package com.example.app.member.command;

import com.example.app.member.entity.Balance;
import com.example.app.member.entity.Member;
import com.example.app.member.repository.BalanceRepository;
import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.request.CreateMemberRequest;
import com.example.app.shared.response.CreateMemberResponse;
import com.example.app.shared.response.embedded.BalanceResponse;
import com.example.app.shared.response.embedded.MemberResponse;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.redis.api.RedisRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateMemberCommandImpl implements CreateMemberCommand {
  private final MemberRepository memberRepository;

  private final BalanceRepository balanceRepository;

  private final RedisRepository repository;

  private final TransactionalOperator transactionalOperator;

  private static final long DEFAULT_BALANCE = 0l;

  @Override
  public Mono<CreateMemberResponse> execute(CreateMemberRequest request) {
    return memberRepository.save(toMember(request))
        .flatMap(this::createBalance)
        .as(transactionalOperator::transactional)
        .doOnSuccess(this::saveToRedis);
  }

  private Member toMember(CreateMemberRequest request) {
    Member member = ConverterHelper.copy(request, Member::new);
    member.setId(IdentifierGenerator.generateId());
    return member;
  }

  private Mono<CreateMemberResponse> createBalance(Member member) {
    return Mono.zip(
        balanceRepository.save(toBalance(member, BalanceType.TOPUP_BALANCE)),
        balanceRepository.save(toBalance(member, BalanceType.CASHOUT_BALANCE)),
        balanceRepository.save(toBalance(member, BalanceType.CASHBACK_BALANCE))
    )
        .map(result -> List.of(result.getT1(), result.getT2(), result.getT3()))
        .map(result -> toResponse(member, result));
  }

  private Balance toBalance(Member member, BalanceType type) {
    return Balance.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .amount(DEFAULT_BALANCE)
        .type(type)
        .build();
  }

  private CreateMemberResponse toResponse(Member member, List<Balance> balances) {
    List<BalanceResponse> balanceResponses = new ArrayList<>();

    for (Balance balance : balances) {
      BalanceResponse balanceResponse = ConverterHelper.copy(balance, BalanceResponse::new);
      balanceResponse.setType(balance.getType().toString());
      balanceResponses.add(balanceResponse);
    }

    return CreateMemberResponse.builder()
        .member(ConverterHelper.copy(member, MemberResponse::new))
        .balances(balanceResponses)
        .build();
  }

  private void saveToRedis(CreateMemberResponse response) {
    MemberResponse memberResponse = response.getMember();
    repository.save(memberResponse.getId(), memberResponse, Duration.ofMinutes(5))
        .subscribe();
  }
}
