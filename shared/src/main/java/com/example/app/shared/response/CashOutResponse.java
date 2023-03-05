package com.example.app.shared.response;

import com.example.app.shared.response.embedded.BalanceResponse;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashOutResponse {
  private String referenceId;
  private String phoneNumber;
  private Map<String, BalanceResponse> balances;
}
