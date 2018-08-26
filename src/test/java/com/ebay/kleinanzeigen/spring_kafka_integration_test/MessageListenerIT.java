package com.ebay.kleinanzeigen.spring_kafka_integration_test;

import static com.ebay.kleinanzeigen.spring_kafka_integration_test.MessageListener.GROUP_ID;
import static com.ebay.kleinanzeigen.spring_kafka_integration_test.MessageListener.TOPIC;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@IntegrationTest
@RunWith(SpringRunner.class)
public class MessageListenerIT {

  private static final TopicPartition PARTITION = new TopicPartition(TOPIC, 0);
  private static final String KEY = randomUUID().toString();

  @Autowired
  private ConsumerFactory<String, String> consumerFactory;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @Autowired
  private MessageRepository messageRepository;

  private Consumer<String, String> consumer;

  private long committedOffsetBeforeConsumingMessages;

  @Test
  public void shouldSaveMessages() {
    givenKafkaConsumer();
    givenCommittedOffsetAfterConsumingPreviousMessages();

    sendMessages("first_message", "second_message", "third_message", "fourth_message");

    assertThatOffsetWasCommittedFor("first_message", "second_message", "third_message", "fourth_message");
    assertThatMessagesHaveBeenSaved("first_message", "second_message", "third_message", "fourth_message");
  }

  private void givenKafkaConsumer() {
    consumer = consumerFactory.createConsumer(GROUP_ID, randomUUID().toString());
  }

  private void givenCommittedOffsetAfterConsumingPreviousMessages() {
    await().until(() -> getCommittedOffset() == getEndOffset());

    committedOffsetBeforeConsumingMessages = getCommittedOffset();
  }

  private void sendMessages(String... messages) {
    for (String message : messages) {
      kafkaTemplate.send(TOPIC, KEY, message);
    }
  }

  private void assertThatOffsetWasCommittedFor(String... messages) {
    await().until(() -> getCommittedOffset() - committedOffsetBeforeConsumingMessages == messages.length);
  }

  private void assertThatMessagesHaveBeenSaved(String... messages) {
    assertThat(messageRepository.findMessages(KEY)).containsExactly(messages);
  }

  private long getCommittedOffset() {
    return consumer.committed(PARTITION).offset();
  }

  private Long getEndOffset() {
    return consumer.endOffsets(singletonList(new TopicPartition(TOPIC, 0))).get(new TopicPartition(TOPIC, 0));
  }

}