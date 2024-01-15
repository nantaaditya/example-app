package com.example.app.member.command;

import com.example.app.member.entity.Balance;
import com.example.app.member.entity.Member;
import com.example.app.member.repository.BalanceRepository;
import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.kafka.CreateBalanceEvent;
import com.example.app.shared.model.kafka.CreateMemberEvent;
import com.example.app.shared.model.kafka.KafkaTopic;
import com.example.app.shared.request.CreateMemberRequest;
import com.example.app.shared.response.CreateMemberResponse;
import com.example.app.shared.response.embedded.BalanceResponse;
import com.example.app.shared.response.embedded.MemberResponse;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.kafka.model.dto.OutboxDTO;
import com.nantaaditya.framework.kafka.service.OutboxService;
import com.nantaaditya.framework.redis.api.RedisRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateMemberCommandImpl implements CreateMemberCommand {

  private final OutboxService<MemberRepository, Member, String, CreateMemberEvent> memberOutboxService;

  private final OutboxService<BalanceRepository, Balance, String, CreateBalanceEvent> balanceOutboxService;

  private final RedisRepository repository;

  private final TransactionalOperator transactionalOperator;

  private String memberId = null;

  private Function<Member, OutboxDTO<CreateMemberEvent>> memberFunction = (member) -> {
    CreateMemberEvent createMemberEvent = ConverterHelper.copy(member, CreateMemberEvent::new);
    return OutboxDTO.create(KafkaTopic.CREATE_MEMBER, createMemberEvent.getId(), createMemberEvent.getId(), createMemberEvent);
  };

  private Function<Balance, OutboxDTO<CreateBalanceEvent>> balanceFunction = (balance) -> {
    CreateBalanceEvent createBalanceEvent = ConverterHelper.copy(balance, CreateBalanceEvent::new);
    createBalanceEvent.setMemberId(memberId);
    createBalanceEvent.setType(balance.getType().name());
    return OutboxDTO.create(KafkaTopic.CREATE_BALANCE, createBalanceEvent.getUniqueId(), createBalanceEvent.getId(), createBalanceEvent);
  };

  @Override
  public Mono<CreateMemberResponse> execute(CreateMemberRequest request) {
    return memberOutboxService.save(toMember(request), memberFunction)
        .flatMap(this::createBalance)
        .as(transactionalOperator::transactional)
        .flatMap(this::saveToRedis);
  }

  private Member toMember(CreateMemberRequest request) {
    Member member = ConverterHelper.copy(request, Member::new);
    member.setId(IdentifierGenerator.generateId());
    memberId = member.getId();
    return member;
  }

  private Mono<CreateMemberResponse> createBalance(Member member) {
    return Mono.zip(
        balanceOutboxService.save(Balance.from(member, BalanceType.TOPUP_BALANCE), balanceFunction),
        balanceOutboxService.save(Balance.from(member, BalanceType.CASHOUT_BALANCE), balanceFunction),
        balanceOutboxService.save(Balance.from(member, BalanceType.CASHBACK_BALANCE), balanceFunction)
    )
        .map(result -> List.of(result.getT1(), result.getT2(), result.getT3()))
        .map(result -> toResponse(member, result));
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

  private Mono<CreateMemberResponse> saveToRedis(CreateMemberResponse response) {
    MemberResponse memberResponse = response.getMember();
    return repository.save(memberResponse.getId(), memberResponse, Duration.ofMinutes(5))
        .map(result -> response);
  }

}
