package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import com.example.app.shared.constant.TransactionType;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.request.CashOutRequest;
import com.example.app.shared.request.TopUpRequest;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "transactions")
public class Transaction extends BaseEntity {
  private String memberId;
  private String referenceId;
  private long amount;
  private TransactionType type;
  private Map<String, Object> metadata;

  public static Transaction from(TopUpRequest request, Member member) {
    return Transaction.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .referenceId(request.getReferenceId())
        .amount(request.getAmount())
        .type(TransactionType.TOP_UP)
        .metadata(request.getAdditionalInfo())
        .build();
  }

  public static Transaction from(CashOutRequest request, Member member) {
    return Transaction.builder()
        .id(IdentifierGenerator.generateId())
        .memberId(member.getId())
        .referenceId(IdentifierGenerator.generateId())
        .amount(request.getAmount())
        .type(TransactionType.CASH_OUT)
        .build();
  }
}
