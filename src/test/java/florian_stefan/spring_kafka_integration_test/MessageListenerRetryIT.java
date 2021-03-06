package florian_stefan.spring_kafka_integration_test;

import static florian_stefan.spring_kafka_integration_test.MessageListener.GROUP_ID;
import static florian_stefan.spring_kafka_integration_test.MessageListener.TOPIC;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doThrow;

import java.util.Optional;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.Lifecycle;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.context.junit4.SpringRunner;

@IntegrationTest
@RunWith(SpringRunner.class)
public class MessageListenerRetryIT {

  private static final TopicPartition PARTITION = new TopicPartition(TOPIC, 0);
  private static final String KEY = randomUUID().toString();

  @Autowired
  private KafkaListenerEndpointRegistry kafkaListenerRegistry;

  @Autowired
  private ConsumerFactory<String, String> consumerFactory;

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  @SpyBean
  private MessageProcessor messageProcessor;

  private MessageListenerContainer kafkaListener;

  private Consumer<String, String> consumer;

  private long committedOffsetBeforeConsumingMessages;

  @Before
  public void startKafkaListener() {
    kafkaListenerRegistry.getListenerContainers().forEach(Lifecycle::start);
  }

  @After
  public void stopKafkaListener() {
    kafkaListenerRegistry.getListenerContainers().forEach(Lifecycle::stop);
  }

  @Test
  public void shouldNotCommitOffsetOnError() {
    givenKafkaListenerAndConsumer();
    givenCommittedOffsetAfterConsumingPreviousMessages();
    givenExceptionWhenEnrichingMessage("error_message");

    sendMessages("first_message", "second_message", "third_message", "error_message", "fourth_message");

    assertThatKafkaListenerHasStopped();
    assertThatOffsetWasOnlyCommittedFor("first_message", "second_message", "third_message");
  }

  private void givenKafkaListenerAndConsumer() {
    await().until(() -> findKafkaListenerAssignedToGivenPartition().isPresent());

    findKafkaListenerAssignedToGivenPartition().ifPresent(this::setKafkaListener);

    consumer = consumerFactory.createConsumer(GROUP_ID, randomUUID().toString());
  }

  private void givenCommittedOffsetAfterConsumingPreviousMessages() {
    await().until(() -> getCommittedOffset() == getEndOffset());

    committedOffsetBeforeConsumingMessages = getCommittedOffset();
  }

  private void givenExceptionWhenEnrichingMessage(String message) {
    RuntimeException exception = new RuntimeException("The message " + message + " could not be enriched.");

    doThrow(exception).when(messageProcessor).enrichMessage(message);
  }

  private void sendMessages(String... messages) {
    for (String message : messages) {
      kafkaTemplate.send(TOPIC, KEY, message);
    }
  }

  private void assertThatKafkaListenerHasStopped() {
    await().until(() -> !kafkaListener.isRunning());
  }

  private void assertThatOffsetWasOnlyCommittedFor(String... messages) {
    assertThat(getCommittedOffset() - committedOffsetBeforeConsumingMessages).isEqualTo(messages.length);
  }

  private Optional<MessageListenerContainer> findKafkaListenerAssignedToGivenPartition() {
    return kafkaListenerRegistry.getListenerContainers().stream().filter(this::isAssignedToGivenPartition).findFirst();
  }

  private boolean isAssignedToGivenPartition(MessageListenerContainer kafkaListener) {
    return kafkaListener.getAssignedPartitions().contains(PARTITION);
  }

  private void setKafkaListener(MessageListenerContainer kafkaListener) {
    this.kafkaListener = kafkaListener;
  }

  private long getCommittedOffset() {
    return ofNullable(consumer.committed(PARTITION)).map(OffsetAndMetadata::offset).orElse(0L);
  }

  private Long getEndOffset() {
    return consumer.endOffsets(singletonList(new TopicPartition(TOPIC, 0))).get(new TopicPartition(TOPIC, 0));
  }

}