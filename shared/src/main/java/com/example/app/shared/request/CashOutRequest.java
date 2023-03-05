package com.example.app.shared.request;

import com.nantaaditya.framework.command.annotation.Hint;
import com.nantaaditya.framework.command.annotation.Metadata;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CashOutRequest {
  @NotBlank(message = "NotBlank")
  private String memberId;

  @Min(value = 1000, message = "BelowThreshold")
  @Metadata(hints = @Hint(violation = "BelowThreshold", hint = "withdraw minimum 1000"))
  private long amount;
}
