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

The connection factory configuration is controlled using runtime config properties, e.g in your `application.properties` file:

| Config Property           | Description                             | Default                 |
| ------------------------- | --------------------------------------- | ----------------------- |
| quarkus.qpid-jms.url      | Connection URL for the injected factory | "amqp://localhost:5672" |
| quarkus.qpid-jms.username | Optional username to set on the factory |                         |
| quarkus.qpid-jms.password | Optional password to set on the factory |                         |
| quarkus.qpid-jms.wrap     | Allow factory to be wrapped (see below) | false                   |

For full details of the client URL and its related options, consult the configuration documentation on the [Apache Qpid](https://qpid.apache.org/components/jms/) website.

##### Factory wrapping, e.g. Pooling.

The `quarkus.qpid-jms.wrap` setting can allow the Qpid JMS extension to apply a ConnectionFactory wrapper supplied by another extension to the injected ConnectionFactory. For example, setting this value to true and adding an additional dependency on [quarkus-pooled-jms](https://github.com/quarkiverse/quarkus-pooled-jms) to your application allows quarkus-pooled-jms to provide pooling for the injected ConnectionFactory.
