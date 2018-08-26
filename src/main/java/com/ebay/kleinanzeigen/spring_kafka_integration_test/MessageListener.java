package com.ebay.kleinanzeigen.spring_kafka_integration_test;

import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_MESSAGE_KEY;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageListener {

  public static final String GROUP_ID = "message-group";
  public static final String TOPIC = "messages";

  private final MessageProcessor messageProcessor;
  private final MessageRepository messageRepository;

  @KafkaListener(groupId = GROUP_ID, topics = TOPIC)
  public void handleAdScoredEvent(@Header(RECEIVED_MESSAGE_KEY) String key, @Payload String message) {
    String enrichedMessage = messageProcessor.enrichMessage(message);

    messageRepository.saveMessage(key, enrichedMessage);
  }

}
