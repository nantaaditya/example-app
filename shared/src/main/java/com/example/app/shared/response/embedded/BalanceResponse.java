package com.example.app.shared.response.embedded;

import com.example.app.shared.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BalanceResponse extends BaseResponse {
  private long amount;

  private String type;
}
