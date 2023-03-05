package com.example.app.member.api;

import com.example.app.member.command.CreateMemberCommand;
import com.example.app.member.command.GetMemberCommand;
import com.example.app.shared.request.CreateMemberRequest;
import com.example.app.shared.request.GetMemberRequest;
import com.example.app.shared.response.CreateMemberResponse;
import com.example.app.shared.response.embedded.MemberResponse;
import com.nantaaditya.framework.command.executor.CommandExecutor;
import com.nantaaditya.framework.rest.model.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final CommandExecutor commandExecutor;

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Response<CreateMemberResponse>> create(@RequestBody CreateMemberRequest request) {
    return commandExecutor.execute(CreateMemberCommand.class, request)
        .map(Response::ok);
  }

  @GetMapping(
      value = "/{memberId}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Response<MemberResponse>> find(@PathVariable String memberId) {
    return commandExecutor.execute(GetMemberCommand.class, GetMemberRequest.builder().id(memberId).build())
        .map(Response::ok);
  }
}
