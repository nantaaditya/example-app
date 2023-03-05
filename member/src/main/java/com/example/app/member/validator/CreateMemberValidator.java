package com.example.app.member.validator;

import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.request.CreateMemberRequest;
import com.nantaaditya.framework.command.model.dto.CommandValidationResult;
import com.nantaaditya.framework.command.validation.BusinessCommandValidator;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CreateMemberValidator implements BusinessCommandValidator<CreateMemberRequest> {

  @Autowired
  private MemberRepository memberRepository;

  @Override
  public Mono<CommandValidationResult> isValid(CreateMemberRequest request) {
    return memberRepository.existsByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber())
        .filter(BooleanUtils::isTrue)
        .doOnNext(result -> log.warn("#VALIDATOR - CreateMemberValidator not passed"))
        .map(result -> toResult());
  }

  private CommandValidationResult toResult() {
    return new CommandValidationResult(
        false,
        Collections.singletonMap("member", Collections.singleton("AlreadyExist")),
        Collections.singletonMap("member", Collections.singletonMap("AlreadyExist", Collections.singleton("email / phone number already registered")))
        );
  }
}
