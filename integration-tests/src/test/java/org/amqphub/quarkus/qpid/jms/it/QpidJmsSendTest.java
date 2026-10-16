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
import static org.amqphub.quarkus.qpid.jms.it.QpidJmsTestSupport.SERVER_PORT;

import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.Response.Status;

import org.apache.qpid.protonj2.test.driver.ProtonTestServer;
import org.apache.qpid.protonj2.test.driver.ProtonTestServerOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class QpidJmsSendTest {

    /**
     * Tests that sending works in the {@link QpidJmsSend} application code using
     * the extension, by doing a POST to the {@link QpidJmsEndpoint} to trigger
     * the app sending a message to the test peer with the POSTed value as its body
     * which the peer will verify.
     *
     * @throws Exception
     *             if there is an unexpected problem
     */
    @Test
    public void testSend() throws Exception {
        final String body = QpidJmsTestSupport.generateBody();

        try (ProtonTestServer peer = new ProtonTestServer(new ProtonTestServerOptions().setServerPort(SERVER_PORT))) {
            peer.expectSASLAnonymousConnect();
            peer.expectOpen().respond();
            peer.expectBegin().respond();
            peer.expectBegin().respond();
            peer.expectAttach().ofSender().respond();
            peer.remoteFlow().withDeliveryCount(0)
                             .withLinkCredit(10)
                             .withIncomingWindow(1024)
                             .withOutgoingWindow(10)
                             .withNextIncomingId(0)
                             .withNextOutgoingId(1).queue();
            peer.expectTransfer().withMessage().withHeader().withDurability(true).also()
                                               .withProperties().withTo(QpidJmsSend.QUEUE_NAME).also()
                                               .withMessageAnnotations().also()
                                               .withValue(body).also()
                                               .accept();
            peer.expectEnd().respond();
            peer.expectClose().respond();

            peer.start();

            Response response = RestAssured.with().body(body).post(ENDPOINT_PATH);
            Assertions.assertEquals(Status.NO_CONTENT.getStatusCode(), response.statusCode());

            peer.waitForScriptToComplete(5, TimeUnit.SECONDS);
        }
    }
}
