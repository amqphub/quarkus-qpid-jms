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

import static org.amqphub.quarkus.qpid.jms.it.QpidJmsTestSupport.ENDPOINT_PATH;

import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Queue;
import jakarta.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
public class QpidJmsReceiveTest {

    /**
     * Tests that receiving works in the {@link QpidJmsReceive} application code
     * using the extension, by sending a message to the broker and using the
     * {@link QpidJmsEndpoint} to trigger the app receiving it from the broker
     * and returning the body, finally comparing the response to that we sent.
     *
     * @throws Exception
     *             if there is an unexpected problem
     */
    @Test
    public void testReceive() throws Exception {
        String body = QpidJmsTestSupport.generateBody();

        try (JMSContext context = QpidJmsTestSupport.createContext()) {
            Queue destination = context.createQueue(QpidJmsReceive.QUEUE_NAME);
            JMSProducer producer = context.createProducer();

            producer.send(destination, body);
        }

        Response response = RestAssured.with().body(body).get(ENDPOINT_PATH);
        Assertions.assertEquals(Status.OK.getStatusCode(), response.statusCode());

        Assertions.assertEquals(body, response.getBody().asString(), "Received body did not match that sent");
    }
}
