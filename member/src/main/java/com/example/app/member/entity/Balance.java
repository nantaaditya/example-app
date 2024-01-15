package com.example.app.member.entity;


import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
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

  private static final long DEFAULT_BALANCE = 0l;

  private String memberId;

  private long amount;

  private BalanceType type;

  public static Balance from(UpdateBalanceEvent updateBalanceEvent) {
    Balance balance = ConverterHelper.copy(updateBalanceEvent, Balance::new);
    balance.setType(BalanceType.of(updateBalanceEvent.getType()));
    return balance;
  }

  public static Balance from(Member member, BalanceType type) {
    return Balance.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .amount(DEFAULT_BALANCE)
        .type(type)
        .build();
  }
}
