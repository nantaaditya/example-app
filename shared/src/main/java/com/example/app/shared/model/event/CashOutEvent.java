package com.example.app.shared.model.event;

public record CashOutEvent(
    String memberId,
    long transactionAmount,
    String transactionId,
    String referenceId
) { }
