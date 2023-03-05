package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.constant.BalanceType;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder
@Table(value = "balance_histories")
public class BalanceHistory extends BaseEntity {

  public enum BalanceAction {
    CREDIT, DEBIT
  }

  private String memberId;
  private String transactionId;
  private BalanceType type;
  private long amount;
  private BalanceAction action;
}
