package com.example.app.shared.response;

import com.example.app.shared.response.embedded.BalanceResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopUpResponse {
  private String referenceId;
  private String phoneNumber;
  private String email;
  private String name;
  private BalanceResponse topUp;
}
