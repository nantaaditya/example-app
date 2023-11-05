package com.example.app.shared.constant;

public enum BalanceType {
  TOPUP_BALANCE,
  CASHOUT_BALANCE,
  CASHBACK_BALANCE;

  public static BalanceType of(String type) {
    BalanceType balanceType = null;
    for (BalanceType bt : values()) {
      if (bt.name().equals(type)) {
        balanceType = bt;
        break;
      }
    }

    return balanceType;
  }
}
