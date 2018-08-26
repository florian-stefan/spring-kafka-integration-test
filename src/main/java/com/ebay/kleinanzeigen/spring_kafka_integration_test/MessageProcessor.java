package com.ebay.kleinanzeigen.spring_kafka_integration_test;

import org.springframework.stereotype.Component;

@Component
public class MessageProcessor {

  private static final String PREFIX = "<";
  private static final String SUFFIX = ">";

  public String enrichMessage(String message) {
    return PREFIX + message + SUFFIX;
  }

}
