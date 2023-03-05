package com.example.app.shared.request;

import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetMemberRequest {
  @NotBlank(message = "NotBlank")
  private String id;
}
