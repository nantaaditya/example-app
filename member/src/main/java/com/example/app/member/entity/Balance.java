package com.example.app.member.entity;


import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.constant.BalanceType;
import com.example.app.shared.model.kafka.UpdateBalanceEvent;
import com.nantaaditya.framework.helper.json.JsonHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;
import reactor.kafka.receiver.ReceiverRecord;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "balances")
public class Balance extends BaseEntity {

  private String memberId;

  private long amount;

  private BalanceType type;

  public static Balance from(ReceiverRecord<String, String> balanceEvent, JsonHelper jsonHelper) {
    UpdateBalanceEvent event = jsonHelper.fromJson(balanceEvent.value(), UpdateBalanceEvent.class);
    return Balance.builder()
        .id(balanceEvent.key())
        .memberId(event.getMemberId())
        .type(BalanceType.of(event.getType()))
        .amount(event.getAmount())
        .build();
  }
}
