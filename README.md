# Quarkus Qpid JMS Extension

An extension which allows using the AMQP 1.0 [Apache Qpid JMS](https://qpid.apache.org/components/jms/) client in a [Quarkus](https://quarkus.io) application, including native executable builds.

### Details

To use the extension, add the org.amqphub.quarkus:quarkus-qpid-jms module as a dependency in your project, e.g:

    <dependency>
        <groupId>org.amqphub.quarkus</groupId>
        <artifactId>quarkus-qpid-jms</artifactId>
    </dependency>

You can then utilise the client via dependency injection of a JMS ConnectionFactory, e.g:

    @Inject
    ConnectionFactory connectionFactory;

The connection factory configuration is controlled using 3 config properties in your `application.properties` file:

| Config Property Name      | Required | Description                             |
| ------------------------- | -------- | --------------------------------------- |
| quarkus.qpid-jms.url      | Yes      | Connection URL for the injected factory |
| quarkus.qpid-jms.username | No       | Optional username to set on the factory |
| quarkus.qpid-jms.password | No       | Optional password to set on the factory |

For details of the client URL format and options, consult the documentation on the [Apache Qpid](https://qpid.apache.org/components/jms/) website.

### Sample Usage

See the [quarkus-qpid-jms-quickstart](https://github.com/amqphub/quarkus-qpid-jms-quickstart/) repository for sample application usage of the extension.
