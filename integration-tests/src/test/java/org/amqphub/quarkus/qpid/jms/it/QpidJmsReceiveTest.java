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

import jakarta.ws.rs.core.Response.Status;

import org.apache.qpid.protonj2.test.driver.ProtonTestServer;
import org.apache.qpid.protonj2.test.driver.ProtonTestServerOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class QpidJmsReceiveTest {

    /**
     * Tests that receiving works in the {@link QpidJmsReceive} application code
     * using the extension, by using a GET from the {@link QpidJmsEndpoint} to trigger
     * the app receiving a message from the test peer and returning its body,
     * finally comparing the response to the body sent by the peer.
     *
     * @throws Exception
     *             if there is an unexpected problem
     */
    @Test
    public void testReceive() throws Exception {
        final String body = QpidJmsTestSupport.generateBody();

        try (ProtonTestServer peer = new ProtonTestServer(new ProtonTestServerOptions().setServerPort(SERVER_PORT))) {
            peer.expectSASLAnonymousConnect();
            peer.expectOpen().respond();
            peer.expectBegin().respond();
            peer.expectBegin().respond();
            peer.expectAttach().ofReceiver().withSource().withAddress(QpidJmsReceive.QUEUE_NAME).also().respond();
            peer.expectFlow().withLinkCredit(1000);
            peer.remoteTransfer().withHandle(0)
                                 .withDeliveryId(0)
                                 .withDeliveryTag(new byte[] { 1 })
                                 .withMore(false)
                                 .withMessageFormat(0)
                                 .withMessage().withBody().withString(body)
                                 .also()
                                 .queue();
            peer.expectDisposition().withFirst(0)
                                    .withSettled(true)
                                    .withState().accepted();
            peer.expectEnd().respond();
            peer.expectClose().respond();

            peer.start();

            Response response = RestAssured.get(ENDPOINT_PATH);
            Assertions.assertEquals(Status.OK.getStatusCode(), response.statusCode());

            Assertions.assertEquals(body, response.getBody().asString(), "Received body did not match that sent by peer");

            peer.waitForScriptToComplete();
        }
    }
}
