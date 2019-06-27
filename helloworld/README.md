# Helloworld Sample

A sample HelloWorld [Quarkus](https://quarkus.io) application using AMQP 1.0
[Apache Qpid JMS](https://qpid.apache.org/components/jms/) client,
periodically sending and receiving a message then printing it to the console.

The sample requires:
* An AMQP 1.0 server at localhost:5672 either offering ANONYMOUS SASL, or configured for "guest/guest" login.
* A queue/topic address named "examples" which exists or can be auto-created on use.
* [GraalVM](https://www.graalvm.org/) 19.0.0+ installed with `GRAALVM_HOME` set, for native builds.
* Maven 3.5.3+

### Build & Run

Be sure to build and install the extension from the parent dir if building from SNAPSHOT sources.

To build and run in JVM mode:

    mvn clean package
    java -jar ./target/helloworld-<version>-runner.jar

To build and run in native build mode:

    mvn clean package -Pnative
    ./target/helloworld-<version>-runner
