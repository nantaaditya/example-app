package com.example.app.core.api;

import com.example.app.core.utils.BalanceAuditService;
import com.nantaaditya.framework.audit.model.request.GetChangelogRequest;
import com.nantaaditya.framework.audit.model.response.GetChangelogResponse;
import com.nantaaditya.framework.rest.model.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/internal-api/audit")
@RequiredArgsConstructor
public class InternalAuditController {

  private final BalanceAuditService balanceAuditService;

  @PostMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public Mono<Response<List<GetChangelogResponse>>> find(@RequestBody GetChangelogRequest request) {
    return balanceAuditService.find(request)
        .map(Response::ok);
  }
}
