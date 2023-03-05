package com.example.app.shared.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateMemberRequest {
  @NotBlank(message = "NotBlank")
  @Email(message = "MustValid")
  private String email;

  @NotBlank(message = "NotBlank")
  private String phoneNumber;

  @NotBlank(message = "NotBlank")
  private String name;
}
