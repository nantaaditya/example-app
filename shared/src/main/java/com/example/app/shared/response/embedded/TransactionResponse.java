package com.example.app.shared.response.embedded;

import com.example.app.shared.base.BaseResponse;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse extends BaseResponse {
  private String referenceId;
  private long amount;
  private String type;
  private Map<String, Object> additionalInfo;
}
