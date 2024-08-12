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
package org.amqphub.quarkus.qpid.jms.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.qpid-jms")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface QpidJmsRuntimeConfig {

   String DEFAULT_URL = "amqp://localhost:5672";

    /**
     * Connection URL for the factory
     */
    @WithDefault(DEFAULT_URL)
    String url();

    /**
     * Username to optionally be set on the factory
     */
    Optional<String> username();

    /**
     * Password to optionally be set on the factory
     */
    Optional<String> password();

    /**
     * Whether to wrap a ConnectionFactory by ConnectionFactoryWrapper which could be introduced by other extensions,
     * such as quarkus-pooled-jms to provide pooling capability
     */
    @WithDefault("false")
    boolean wrap();
}
