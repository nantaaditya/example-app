package com.example.app.core.validator;

import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.request.CashOutRequest;
import com.nantaaditya.framework.command.model.dto.CommandValidationResult;
import com.nantaaditya.framework.command.validation.BusinessCommandValidator;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawValidator implements BusinessCommandValidator<CashOutRequest> {

  private final MemberRepository memberRepository;

  private final BalanceRepository balanceRepository;

  @Override
  public Mono<CommandValidationResult> isValid(CashOutRequest cashOutRequest) {
    return validatePhoneNumberIsExists(cashOutRequest)
        .switchIfEmpty(Mono.defer(() -> validaBalanceIsEnough(cashOutRequest)));
  }

  private Mono<CommandValidationResult> validatePhoneNumberIsExists(CashOutRequest request) {
    return memberRepository.existsById(request.getMemberId())
        .filter(BooleanUtils::isFalse)
        .doOnNext(result -> log.warn("#VALIDATOR - phone number not exists "))
        .map(result -> toError("memberId", "NotExists", "member not exists"));
  }

  private Mono<CommandValidationResult> validaBalanceIsEnough(CashOutRequest request) {
    return memberRepository.findById(request.getMemberId())
        .flatMap(member -> balanceRepository.findByTypeAndMemberId(BalanceType.TOPUP_BALANCE, member.getId()))
        .filter(Objects::nonNull)
        .filter(balance -> balance.getAmount() - request.getAmount() < 0)
        .doOnNext(result -> log.warn("#VALIDATOR - balance not enough"))
        .map(result -> toError("balance", "NotEnough", "balance is not enough to withdraw"));
  }
}
