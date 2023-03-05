package com.example.app.shared.response;

import com.example.app.shared.response.embedded.BalanceResponse;
import com.example.app.shared.response.embedded.MemberResponse;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateMemberResponse {
  private MemberResponse member;
  private List<BalanceResponse> balances;
}
