package com.example.app.core.entity;

import com.example.app.shared.base.BaseEntity;
import java.util.Arrays;
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

  public enum TransactionType {
    TOP_UP, TRANSFER, CASH_OUT;

    public static TransactionType of(String type) {
      return Arrays.asList(values())
          .stream()
          .filter(transactionType -> transactionType.name().equals(type))
          .findFirst()
          .orElse(null);
    }
  }

  private String memberId;
  private String referenceId;
  private long amount;
  private TransactionType type;
  private Map<String, Object> metadata;
}
