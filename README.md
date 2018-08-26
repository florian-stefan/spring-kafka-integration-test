# Spring Kafka Integration Test Example

This repository contains example code for setting up an integration test for Spring Kafka using a Kafka broker running
in a Docker container and for demonstrating a potential pitfall when using the annotations `@MockBean` or `@SpyBean`.

### Integration Tests using Docker

This project uses the [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) to start a Kafka broker
with a preconfigured topic. Furthermore, the project uses the [kafka-docker](https://github.com/wurstmeister/kafka-docker)
Docker images to run Zookeeper and Kafka inside Docker containers. The containers can be started resp. stopped by navigating
to the root folder of this project and running the following commands:

* `mvn docker:build docker:start` (to start the Docker containers)
* `mvn docker:stop` (to stop and remove the Docker containers)

The Docker image used for running Kafka offers the possibility to preconfigure topics. This is done by specifying the
environment variable `KAFKA_CREATE_TOPICS` whose value should follow the syntax `<topicName>:<partitions>:<replicas>`.
This project uses the configuration `<KAFKA_CREATE_TOPICS>messages:1:1</KAFKA_CREATE_TOPICS>` which means that a topic
will be created with the name `messages`, one partition and a replication factor of one. 

### Application using Spring Kafka

The example application uses [Spring Kafka](https://spring.io/projects/spring-kafka) to consume messages from a Kafka
topic and for storing them in a `HashMap`. The `KafkaListenerContainerFactory` is configured to only commit the offset
when the listener handles the message successfully, i.e. does not throw an exception, and to stop the listener whenever
an error occurs. Furthermore, the group id used by the `MessageListener` has the fixed value `message-group` which means
that all instances of this listener connected to the same Kafka cluster will be part of the same consumer group. Since
the topic that is used for the integration tests has only one partition, that also implies that - when several Spring
contexts exists in parallel during integration testing - at most one Spring context will contain a non-idle instance of
that listener.