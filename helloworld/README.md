# Helloworld Sample

A sample HelloWorld [Quarkus](https://quarkus.io) application using AMQP 1.0
[Apache Qpid JMS](https://qpid.apache.org/components/jms/) client,
periodically sending and receiving a message then printing it to the console.

The sample requires:
* An AMQP 1.0 server at localhost:5672 either offering ANONYMOUS SASL, or configured for "guest/guest" login.
* A queue/topic address named "examples" which exists or can be auto-created on use.
* Maven 3.5.3+
* For native builds, [GraalVM](https://www.graalvm.org/) version [19.2.0.1](https://github.com/oracle/graal/releases/tag/vm-19.2.0.1) [installed](https://www.graalvm.org/docs/getting-started), with `GRAALVM_HOME` set and [native-image extension](https://www.graalvm.org/docs/reference-manual/aot-compilation/).

### Build & Run

Be sure to build and install the extension from the parent dir if building from SNAPSHOT sources.

To build and run in JVM mode:

    mvn clean package
    java -jar ./target/helloworld-<version>-runner.jar

To build and run in native build mode:

    mvn clean package -Pnative
    ./target/helloworld-<version>-runner
