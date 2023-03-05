package com.example.app.member.validator;

import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.request.GetMemberRequest;
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
public class GetMemberValidator implements BusinessCommandValidator<GetMemberRequest> {

  @Autowired
  private MemberRepository memberRepository;

  @Override
  public Mono<CommandValidationResult> isValid(GetMemberRequest request) {
    return memberRepository.existsById(request.getId())
        .filter(BooleanUtils::isFalse)
        .doOnNext(result -> log.warn("#VALIDATOR - GetMemberValidator not passed"))
        .map(result -> toResult());
  }

  private CommandValidationResult toResult() {
    return new CommandValidationResult(
        false,
        Collections.singletonMap("member", Collections.singleton("NotExists")),
        Collections.singletonMap("member", Collections.singletonMap("NotExists", Collections.singleton("member not exists")))
    );
  }
}
