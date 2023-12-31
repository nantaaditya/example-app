package com.example.app.shared.constant;

import java.util.Arrays;

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
