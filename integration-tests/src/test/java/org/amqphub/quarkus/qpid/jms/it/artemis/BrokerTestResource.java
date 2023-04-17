/*
* Copyright 2023 the original author or authors.
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
package org.amqphub.quarkus.qpid.jms.it.artemis;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.commons.io.FileUtils;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class BrokerTestResource implements QuarkusTestResourceLifecycleManager {

    private EmbeddedActiveMQ embeddedBroker;

    @Override
    public Map<String, String> start() {
        try {
            // Matches what is defined in the broker.xml file in-tree.
            File parentDir = Paths.get("./target/artemis").toFile();
            FileUtils.deleteDirectory(parentDir);

            embeddedBroker = new EmbeddedActiveMQ();
            embeddedBroker.start();
        } catch (Exception e) {
            throw new RuntimeException("Problem starting embedded ActiveMQ Artemis broker", e);
        }

        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (embeddedBroker == null) {
            return;
        }

        try {
            embeddedBroker.stop();
        } catch (Exception e) {
            throw new RuntimeException("Problem stopping embedded ActiveMQ Artemis broker", e);
        }
    }
}