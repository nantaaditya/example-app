package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.constant.BalanceAction;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder
@Table(value = "balance_histories")
public class BalanceHistory extends BaseEntity {

  private String memberId;
  private String transactionId;
  private BalanceType type;
  private long amount;
  private BalanceAction action;

  public static BalanceHistory from(String memberId, String transactionId, long amount,
      BalanceType balanceType, BalanceAction action) {
    return BalanceHistory.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(memberId)
        .amount(amount)
        .type(balanceType)
        .action(action)
        .transactionId(transactionId)
        .build();
  }
}
