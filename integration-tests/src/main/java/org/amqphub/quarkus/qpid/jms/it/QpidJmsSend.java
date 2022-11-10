/*
* Copyright 2020 the original author or authors.
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
package org.amqphub.quarkus.qpid.jms.it;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Queue;

@ApplicationScoped
public class QpidJmsSend {

    static final String QUEUE_NAME = "test-qpid-jms-send";

    @Inject
    ConnectionFactory connectionFactory;


    public void sendMessageBody(String body) {
        try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            Queue destination = context.createQueue(QUEUE_NAME);
            JMSProducer producer = context.createProducer();

            producer.send(destination, body);
        }
    }
}
