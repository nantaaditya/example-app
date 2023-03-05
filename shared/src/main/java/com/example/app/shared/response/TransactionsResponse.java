package com.example.app.shared.response;

import com.example.app.shared.response.embedded.TransactionResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsResponse {

  private String email;
  private String phoneNumber;
  private String name;
  private List<TransactionResponse> transactions;

  private int page;
  private int totalPage;
  private long totalData;

}
