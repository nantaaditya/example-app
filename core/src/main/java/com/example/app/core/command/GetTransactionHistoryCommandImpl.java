package com.example.app.core.command;

import com.example.app.core.entity.Member;
import com.example.app.core.entity.Transaction;
import com.example.app.core.repository.MemberRepository;
import com.example.app.core.repository.TransactionRepository;
import com.example.app.core.utils.DtoConverter;
import com.example.app.shared.constant.TransactionType;
import com.example.app.shared.request.GetTransactionHistoryRequest;
import com.example.app.shared.response.TransactionsResponse;
import java.util.List;
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
        .map(result -> DtoConverter.toTransactionsResponse(request, result.getT1(), result.getT2(), result.getT3()));
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

}
