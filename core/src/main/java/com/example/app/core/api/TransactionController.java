package com.example.app.core.api;

import com.example.app.core.command.CashOutCommand;
import com.example.app.core.command.GetTransactionHistoryCommand;
import com.example.app.core.command.TopUpCommand;
import com.example.app.shared.request.CashOutRequest;
import com.example.app.shared.request.GetTransactionHistoryRequest;
import com.example.app.shared.request.TopUpRequest;
import com.example.app.shared.response.CashOutResponse;
import com.example.app.shared.response.TopUpResponse;
import com.example.app.shared.response.TransactionsResponse;
import com.nantaaditya.framework.command.executor.CommandExecutor;
import com.nantaaditya.framework.rest.model.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final CommandExecutor commandExecutor;

  @PostMapping(value = "/top-up",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Response<TopUpResponse>> topUp(@RequestBody TopUpRequest request) {
    return commandExecutor.execute(TopUpCommand.class, request)
        .map(Response::ok);
  }

  @PostMapping(value = "/cash-out",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Response<CashOutResponse>> withdraw(@RequestBody CashOutRequest request) {
    return commandExecutor.execute(CashOutCommand.class, request)
        .map(Response::ok);
  }

  @GetMapping(value = "/{memberId}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Response<TransactionsResponse>> history(@PathVariable String memberId,
      @RequestParam(defaultValue = "0", required = false) int page,
      @RequestParam(defaultValue = "10", required = false) int size,
      @RequestParam(defaultValue = "", required = false) String transactionType) {
    return commandExecutor.execute(GetTransactionHistoryCommand.class,
        GetTransactionHistoryRequest.builder().memberId(memberId).page(page).size(size).transactionType(transactionType).build())
        .map(Response::ok);
  }
}
