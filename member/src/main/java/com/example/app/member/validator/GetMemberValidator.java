package com.example.app.member.validator;

import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.request.GetMemberRequest;
import com.nantaaditya.framework.command.model.dto.CommandValidationResult;
import com.nantaaditya.framework.command.validation.BusinessCommandValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GetMemberValidator implements BusinessCommandValidator<GetMemberRequest> {

  @Autowired
  private MemberRepository memberRepository;

  @Override
  public Mono<CommandValidationResult> isValid(GetMemberRequest request) {
    return memberRepository.existsById(request.getId())
        .filter(BooleanUtils::isFalse)
        .doOnNext(result -> log.warn("#VALIDATOR - GetMemberValidator not passed"))
        .map(result -> toError("member", "NotExists", "member not exists"));
  }
}
