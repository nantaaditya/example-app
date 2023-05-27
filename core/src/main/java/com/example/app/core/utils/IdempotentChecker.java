package com.example.app.core.utils;

import com.example.app.core.entity.IdempotentData;
import com.example.app.core.repository.IdempotentDataRepository;
import com.example.app.shared.helper.IdentifierGenerator;
import com.nantaaditya.framework.helper.idempotent.AbstractIdempotentCheckStrategy;
import com.nantaaditya.framework.helper.json.JsonHelper;
import com.nantaaditya.framework.helper.model.IdempotentRecord;
import com.nantaaditya.framework.helper.properties.IdempotentProperties;
import java.util.Map;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

public class IdempotentChecker extends AbstractIdempotentCheckStrategy {

  private IdempotentDataRepository idempotentDataRepository;

  public IdempotentChecker(JsonHelper jsonHelper,
      IdempotentProperties idempotentProperties,
      IdempotentDataRepository idempotentDataRepository) {
    super(jsonHelper, idempotentProperties);
    this.idempotentDataRepository = idempotentDataRepository;
  }

  @Override
  protected Mono<IdempotentRecord> findIdempotentData(String client,
      Map<String, String> requestMap) {
    return idempotentDataRepository.findByKey(jsonHelper.toJson(requestMap))
        .filter(idempotentData -> idempotentData != null)
        .map(idempotentData -> toIdempotentRecord(client, true, requestMap, idempotentData))
        .defaultIfEmpty(toIdempotentRecord(client, false, requestMap, new IdempotentData()));
  }

  @Override
  public Mono<Boolean> removeObsoleteRecord() {
    return idempotentDataRepository.deleteByCreatedTimeLessThan(System.currentTimeMillis())
        .map(item -> Boolean.TRUE)
        .onErrorReturn(Boolean.FALSE);
  }

  private IdempotentRecord toIdempotentRecord(String client, boolean isIdempotent,
      Map<String, String> request, IdempotentData idempotentData) {
    return new IdempotentRecord(
        ObjectUtils.isEmpty(idempotentData) ? IdentifierGenerator.generateId() : idempotentData.getId(),
        isIdempotent,
        client,
        "topup",
        request,
        idempotentData.getValue(),
        System.currentTimeMillis()
    );
  }
}
