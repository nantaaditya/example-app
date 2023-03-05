package com.example.app.shared.model.event;

public record WithdrawEvent(
    String memberId,
    long transactionAmount,
    String transactionId
) { }
