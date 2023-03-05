package com.example.app.core.validator;

import com.example.app.core.entity.Transaction.TransactionType;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.shared.request.TopUpRequest;
import com.nantaaditya.framework.command.model.dto.CommandValidationResult;
import com.nantaaditya.framework.command.validation.BusinessCommandValidator;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopUpValidator implements BusinessCommandValidator<TopUpRequest> {

  private final MemberRepository memberRepository;

  private final TransactionRepository transactionRepository;

  @Override
  public Mono<CommandValidationResult> isValid(TopUpRequest request) {
    return validatePhoneNumberIsExists(request)
        .switchIfEmpty(Mono.defer(() -> validateReferenceIdIsExists(request)));
  }

  private Mono<CommandValidationResult> validatePhoneNumberIsExists(TopUpRequest request) {
    return memberRepository.existsById(request.getMemberId())
        .filter(BooleanUtils::isFalse)
        .doOnNext(result -> log.warn("#VALIDATOR - phone number not exists "))
        .map(result -> toPhoneNumberNotExists());
  }

  private CommandValidationResult toPhoneNumberNotExists() {
    return new CommandValidationResult(
        false,
        Collections.singletonMap("memberId", Collections.singleton("NotExists")),
        Collections.singletonMap("memberId", Collections.singletonMap("NotExists", Collections.singleton("member not exists")))
    );
  }

  private Mono<CommandValidationResult> validateReferenceIdIsExists(TopUpRequest request) {
    return transactionRepository.existsByTypeAndReferenceId(TransactionType.TOP_UP, request.getReferenceId())
        .filter(BooleanUtils::isTrue)
        .doOnNext(result -> log.warn("#VALIDATOR - reference id already exists "))
        .map(result -> toReferenceIdAlreadyExists());
  }

  private CommandValidationResult toReferenceIdAlreadyExists() {
    return new CommandValidationResult(
        false,
        Collections.singletonMap("referenceId", Collections.singleton("MustNotExists")),
        Collections.singletonMap("referenceId", Collections.singletonMap("MustNotExists", Collections.singleton("transaction already exists")))
    );
  }
}
