# Spring Kafka Integration Test Example

This repository contains example code for setting up an integration test for Spring Kafka using a Kafka broker running
in a Docker container and for demonstrating a potential pitfall when using the annotations `@MockBean` or `@SpyBean`.

### Integration Tests using Docker

This project uses the [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) to start a Kafka broker
with a preconfigured topic. Furthermore, the project uses the [kafka-docker](https://github.com/wurstmeister/kafka-docker)
Docker images to run Zookeeper and Kafka inside Docker containers. The containers can started resp. stopped by navigating
to the root folder of this project and running the following commands:

* `mvn docker:build docker:start` (to start the Docker containers)
* `mvn docker:stop` (to stop and remove the Docker containers)

The Docker image used for running Kafka offers the possibility to preconfigure topics. This is done by specifying the
environment variable `KAFKA_CREATE_TOPICS` whose value should follow the syntax `<topicName>:<partitions>:<replicas>`.
This project uses the configuration `<KAFKA_CREATE_TOPICS>messages:1:1</KAFKA_CREATE_TOPICS>` which means that a topic
will be created with the name `messages`, one partition and a replication factor of `1`. 