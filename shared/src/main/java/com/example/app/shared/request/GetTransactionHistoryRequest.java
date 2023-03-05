package com.example.app.shared.request;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetTransactionHistoryRequest {
  @NotBlank(message = "NotBlank")
  private String memberId;

  private String transactionType;

  private int page;

  private int size;
}
