package florian_stefan.spring_kafka_integration_test;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerStoppingErrorHandler;

@EnableKafka
@SpringBootApplication
public class Application {

  private static final String AUTO_OFFSET_RESET_EARLIEST = "earliest";

  private final String bootstrapServers;

  public Application(@Value("${kafka.bootstrap.servers}") String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(consumerFactory());
    factory.getContainerProperties().setAckOnError(false);
    factory.getContainerProperties().setErrorHandler(new ContainerStoppingErrorHandler());

    return factory;
  }

  @Bean
  public ConsumerFactory<String, String> consumerFactory() {
    Map<String, Object> configuration = new HashMap<>();

    configuration.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    configuration.put(AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_EARLIEST);
    configuration.put(ENABLE_AUTO_COMMIT_CONFIG, false);

    return new DefaultKafkaConsumerFactory<>(configuration, new StringDeserializer(), new StringDeserializer());
  }

}
