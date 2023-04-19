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

import io.quarkus.arc.Arc;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import org.apache.qpid.jms.JmsConnectionFactory;

import io.quarkus.arc.DefaultBean;

@ApplicationScoped
public class QpidJmsProducer {

    @Inject
    QpidJmsRuntimeConfig config;

    @Produces
    @ApplicationScoped
    @DefaultBean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory = new JmsConnectionFactory(config.username.orElse(null), config.password.orElse(null), config.url);
        ConnectionFactoryWrapper wrapper = Arc.container().instance(ConnectionFactoryWrapper.class).get();

        if (config.wrap && wrapper != null) {
            return wrapper.wrap(connectionFactory);
        } else {
            return connectionFactory;
        }
    }

    public QpidJmsRuntimeConfig getConfig() {
        return config;
    }
}
