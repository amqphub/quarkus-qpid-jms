/*
* Copyright 2019 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.amqphub.quarkus.example;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class Main {

	@ConfigProperty(name = "connection.uri", defaultValue = "amqp://localhost:5672")
	String connectionUri;
	@ConfigProperty(name = "connection.username", defaultValue = "guest")
	String username;
	@ConfigProperty(name = "connection.password", defaultValue = "guest")
	String password;

	private Connection conn;

	void onStart(@Observes StartupEvent ev) throws Exception {
		System.out.println("Starting");

		final ConnectionFactory fac = new JmsConnectionFactory(connectionUri);
		conn = fac.createConnection(username, password);

		final Session session = conn.createSession(Session.AUTO_ACKNOWLEDGE);
		final Destination dest = session.createQueue("examples");

		final MessageConsumer consumer = session.createConsumer(dest);
		final MessageProducer producer = session.createProducer(dest);

		AtomicInteger count = new AtomicInteger();
		consumer.setMessageListener(msg -> {
			try {
				System.out.println("Received message: " + msg.getBody(String.class));

				Thread.sleep(1000);
				System.out.println("Sending next message");
				TextMessage message = session.createTextMessage("Hello World! " + count.incrementAndGet());
				producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 5000L);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		System.out.println("Sending first message");
		TextMessage message = session.createTextMessage("Hello World! " + count.incrementAndGet());
		producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, 5000L);

		conn.start();
	}

	void onStop(@Observes ShutdownEvent ev) throws Exception {
		System.out.println("Shutting down");
		if(conn != null) {
			conn.close();
		}
	}
}