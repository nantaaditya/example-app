package com.example.app.core.utils;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.shared.helper.IdentifierGenerator;
import com.example.app.shared.request.GetTransactionHistoryRequest;
import com.example.app.shared.request.TopUpRequest;
import com.example.app.shared.response.CashOutResponse;
import com.example.app.shared.response.TopUpResponse;
import com.example.app.shared.response.TransactionsResponse;
import com.example.app.shared.response.embedded.BalanceResponse;
import com.example.app.shared.response.embedded.TransactionResponse;
import com.nantaaditya.framework.audit.model.eventbus.IdempotentRecord;
import com.nantaaditya.framework.audit.model.request.AuditRequest;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import com.nantaaditya.framework.helper.json.JsonHelper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DtoConverter {

  public static AuditRequest<String, Balance> toAuditRequest(long amount, Balance balance,
      String activity, char operator) {
    return new AuditRequest(
        balance.getId(),
        balance.getModifiedBy(),
        balance.getModifiedTime(),
        activity,
        updateBalance(amount, balance, operator)
    );
  }

  public static IdempotentRecord toTopUpIdempotent(Map<String, String> idempotentRequest, TopUpResponse topUpResponse,
      JsonHelper jsonHelper) {
    return new IdempotentRecord(
        IdentifierGenerator.generateId(),
        false,
        "internal",
        "topup",
        idempotentRequest,
        jsonHelper.toJson(topUpResponse),
        System.currentTimeMillis()
    );
  }

  public static TopUpResponse toTopUpResponse(TopUpRequest request, Member member, Balance balance) {
    BalanceResponse balanceResponse = ConverterHelper.copy(balance, BalanceResponse::new);
    balanceResponse.setType(balance.getType().toString());

    return TopUpResponse.builder()
        .referenceId(request.getReferenceId())
        .name(member.getName())
        .phoneNumber(member.getPhoneNumber())
        .email(member.getEmail())
        .topUp(balanceResponse)
        .build();
  }

  public static CashOutResponse toCashOutResponse(Member member, Transaction transaction,
      Balance topUpBalance, Balance withdrawBalance) {
    BalanceResponse topUp = ConverterHelper.copy(topUpBalance, BalanceResponse::new);
    topUp.setType(topUpBalance.getType().toString());
    BalanceResponse withdraw = ConverterHelper.copy(withdrawBalance, BalanceResponse::new);
    withdraw.setType(withdrawBalance.getType().toString());

    return CashOutResponse.builder()
        .phoneNumber(member.getPhoneNumber())
        .referenceId(transaction.getReferenceId())
        .balances(Map.of(
            "topUp", topUp,
            "cashOut", withdraw
        ))
        .build();
  }

  public static TransactionsResponse toTransactionsResponse(GetTransactionHistoryRequest request, Member member,
      List<Transaction> transactions, long total) {
    return TransactionsResponse.builder()
        .name(member.getName())
        .email(member.getEmail())
        .phoneNumber(member.getPhoneNumber())
        .transactions(toResponse(transactions))
        .page(request.getPage())
        .totalPage(total < request.getSize() ? 1 : (int) Math.ceil(total / request.getSize()))
        .totalData(total)
        .build();
  }

  private static List<TransactionResponse> toResponse(List<Transaction> transactions) {
    return transactions.stream()
        .map(t -> {
          TransactionResponse response = ConverterHelper.copy(t, TransactionResponse::new);
          response.setType(t.getType().name());
          response.setAdditionalInfo(t.getMetadata());
          return response;
        })
        .collect(Collectors.toList());
  }

  private static Balance updateBalance(long amount, Balance balance, char operator) {
    if ('+' == operator) balance.increaseBalance(amount);
    else if ('-' == operator) balance.decreaseBalance(amount);
    return balance;
  }
}
