package com.example.app.shared.request;

import java.util.Map;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopUpRequest {
  @NotBlank(message = "NotBlank")
  private String memberId;

  @Min(value = 1, message = "BelowThreshold")
  private long amount;

  @NotBlank(message = "NotBlank")
  private String referenceId;

  private Map<String, Object> additionalInfo;
}
