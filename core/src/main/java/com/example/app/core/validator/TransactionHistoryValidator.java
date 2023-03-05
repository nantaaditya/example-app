package com.example.app.core.validator;

import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.request.GetTransactionHistoryRequest;
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
public class TransactionHistoryValidator implements BusinessCommandValidator<GetTransactionHistoryRequest> {
  @Autowired
  private MemberRepository memberRepository;

  @Override
  public Mono<CommandValidationResult> isValid(GetTransactionHistoryRequest request) {
    return memberRepository.existsById(request.getMemberId())
        .filter(BooleanUtils::isFalse)
        .doOnNext(result -> log.warn("#VALIDATOR - TransactionHistoryValidator not passed"))
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
