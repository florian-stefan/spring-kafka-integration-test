package com.ebay.kleinanzeigen.spring_kafka_integration_test;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MessageRepository {

  private final Map<String, List<String>> messages = new HashMap<>();

  public void saveMessage(String key, String message) {
    if (messages.containsKey(key)) {
      messages.get(key).add(message);
    } else {
      messages.put(key, new ArrayList<>(singletonList(message)));
    }
  }

  public List<String> findMessages(String key) {
    return ofNullable(messages.get(key)).map(Collections::unmodifiableList).orElse(emptyList());
  }

}
