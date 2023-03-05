package com.example.app.shared.response.embedded;

import com.example.app.shared.base.BaseResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class MemberResponse extends BaseResponse {

  private String email;

  private String phoneNumber;

  private String name;

}
