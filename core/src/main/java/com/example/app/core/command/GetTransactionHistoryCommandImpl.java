package com.example.app.core.command;

import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.core.entity.Transaction.TransactionType;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.shared.request.GetTransactionHistoryRequest;
import com.example.app.shared.response.TransactionsResponse;
import com.example.app.shared.response.embedded.TransactionResponse;
import com.nantaaditya.framework.helper.converter.ConverterHelper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetTransactionHistoryCommandImpl implements
    GetTransactionHistoryCommand {

  private final MemberRepository memberRepository;
  private final TransactionRepository transactionRepository;

  @Override
  public Mono<TransactionsResponse> execute(
      GetTransactionHistoryRequest request) {
    return Mono.zip(
        findMember(request),
        findTransactions(request),
        countTransactions(request)
        )
        .map(result -> toResponse(request, result.getT1(), result.getT2(), result.getT3()));
  }

  private Mono<Member> findMember(GetTransactionHistoryRequest request) {
    return memberRepository.findById(request.getMemberId());
  }

  private Mono<List<Transaction>> findTransactions(GetTransactionHistoryRequest request) {
    return transactionRepository.findTransactions(
        request.getMemberId(),
        TransactionType.of(request.getTransactionType()),
        PageRequest.of(request.getPage(), request.getSize())
            .withSort(Sort
                .by("createdTime")
                .descending()
            )
    )
        .collectList();
  }

  private Mono<Long> countTransactions(GetTransactionHistoryRequest request) {
    return transactionRepository.countTransactions(request.getMemberId(), TransactionType.of(request.getTransactionType()));
  }

  private TransactionsResponse toResponse(GetTransactionHistoryRequest request, Member member,
      List<Transaction> transactions, Long total) {
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

  private List<TransactionResponse> toResponse(List<Transaction> transactions) {
    return transactions.stream()
        .map(t -> {
          TransactionResponse response = ConverterHelper.copy(t, TransactionResponse::new);
          response.setType(t.getType().name());
          response.setAdditionalInfo(t.getMetadata());
          return response;
        })
        .collect(Collectors.toList());
  }
}
