# Quarkus Qpid JMS Extension

An extension which facilitates use of the AMQP JMS client from [Apache Qpid](https://qpid.apache.org/components/jms/) as part of a [Quarkus](https://quarkus.io) application, including those using native executable builds.

Use JMS APIs with AMQP 1.0 servers such as ActiveMQ Artemis, ActiveMQ 5, Qpid Broker-J, Qpid Dispatch router, Azure Service Bus, and more.

### Sample Usage

See the [quarkus-qpid-jms-quickstart](https://github.com/amqphub/quarkus-qpid-jms-quickstart/) repository for sample application usage of the extension.

### Overview

To use the extension, add the `org.amqphub.quarkus:quarkus-qpid-jms` module as a dependency in your project, e.g:

    <dependency>
        <groupId>org.amqphub.quarkus</groupId>
        <artifactId>quarkus-qpid-jms</artifactId>
    </dependency>

The client can then be utilised though dependency injection of a JMS ConnectionFactory, e.g:

    @Inject
    ConnectionFactory connectionFactory;

### Configuration

The connection factory configuration is controlled using 3 runtime config properties, e.g in your `application.properties` file:

| Config Property           | Required | Description                             |
| ------------------------- | -------- | --------------------------------------- |
| quarkus.qpid-jms.url      | Yes      | Connection URL for the injected factory |
| quarkus.qpid-jms.username | No       | Optional username to set on the factory |
| quarkus.qpid-jms.password | No       | Optional password to set on the factory |

An example URL for getting started might be `amqp://localhost:5672`.

For full details of the client URL, consult the configuration documentation on the [Apache Qpid](https://qpid.apache.org/components/jms/) website.
