package com.example.app.core.entity;


import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.constant.BalanceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "balances")
public class Balance extends BaseEntity {

  private String memberId;

  private long amount;

  private BalanceType type;

  public void increaseBalance(long creditAmount) {
    this.amount += creditAmount;
  }

  public void decreaseBalance(long debitAmount) {
    this.amount -= debitAmount;
  }
}
