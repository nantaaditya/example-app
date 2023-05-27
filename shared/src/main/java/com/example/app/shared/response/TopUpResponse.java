package com.example.app.shared.response;

import com.example.app.shared.response.embedded.BalanceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpResponse {
  private String referenceId;
  private String phoneNumber;
  private String email;
  private String name;
  private boolean idempotent;
  private BalanceResponse topUp;
}
