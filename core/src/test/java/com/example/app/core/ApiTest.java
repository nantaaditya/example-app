package com.example.app.core;

import com.example.app.core.entity.Balance;
import com.example.app.core.entity.Member;
import com.example.app.core.repository.BalanceRepository;
import com.example.app.core.repository.MemberRepository;
import com.example.app.shared.constant.BalanceType;
import com.github.f4b6a3.tsid.TsidCreator;
import com.nantaaditya.framework.test.service.RestApiIntegrationService;
import java.util.List;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@Slf4j
@SpringBootTest(
    classes = CoreApplication.class,
    webEnvironment = WebEnvironment.DEFINED_PORT
)
public class ApiTest {

  @Autowired
  private RestApiIntegrationService restApiIntegrationService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BalanceRepository balanceRepository;

  @BeforeEach
  public void setUp() {
    String memberId = "0BJHA40BYE9W7";
    Member member = Member.builder()
        .id(memberId)
        .email("user@mail.com")
        .phoneNumber("081234567890")
        .name("user")
        .build();
    memberRepository.save(member).subscribe();

    Balance topUpBalance = Balance.builder()
        .id(TsidCreator.getTsid256().toString())
        .memberId(memberId)
        .amount(0)
        .type(BalanceType.TOPUP_BALANCE)
        .build();
    Balance cashOutBalance = Balance.builder()
        .id(TsidCreator.getTsid256().toString())
        .memberId(memberId)
        .amount(0)
        .type(BalanceType.CASHOUT_BALANCE)
        .build();
    balanceRepository.saveAll(List.of(topUpBalance, cashOutBalance)).subscribe();
  }

  @TestFactory
  public Stream<DynamicTest> core() {
    return restApiIntegrationService.runGroupAPITest("core");
  }

  @AfterEach
  @SneakyThrows
  public void tearDown() {
    memberRepository.deleteAll().subscribe();
    balanceRepository.deleteAll().subscribe();
  }
}
