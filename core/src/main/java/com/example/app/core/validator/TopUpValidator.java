package com.example.app.core.validator;

import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.request.TopUpRequest;
import com.nantaaditya.framework.command.model.dto.CommandValidationResult;
import com.nantaaditya.framework.command.validation.BusinessCommandValidator;
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

  @Override
  public Mono<CommandValidationResult> isValid(TopUpRequest request) {
    return validatePhoneNumberIsExists(request);
  }

  private Mono<CommandValidationResult> validatePhoneNumberIsExists(TopUpRequest request) {
    return memberRepository.existsById(request.getMemberId())
        .filter(BooleanUtils::isFalse)
        .doOnNext(result -> log.warn("#VALIDATOR - phone number not exists "))
        .map(result -> toError("memberId", "NotExists", "member not exists"));
  }

}
