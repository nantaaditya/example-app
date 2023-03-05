package com.example.app.member.command;

import com.example.app.member.repository.MemberRepository;
import com.example.app.shared.request.GetMemberRequest;
import com.example.app.shared.response.embedded.MemberResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetMemberCommandImpl implements GetMemberCommand {

  private final MemberRepository memberRepository;

  @Override
  public Mono<MemberResponse> execute(GetMemberRequest request) {
    return memberRepository.findById(request.getId())
        .map(result -> ConverterHelper.copy(result, MemberResponse::new));
  }

  @Override
  public String getCacheKey(GetMemberRequest request) {
    return request.getId();
  }

  @Override
  public TypeReference<MemberResponse> getResponseClass() {
    return new TypeReference<MemberResponse>() {};
  }
}
