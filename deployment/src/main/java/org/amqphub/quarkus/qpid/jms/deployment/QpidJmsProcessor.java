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
package org.amqphub.quarkus.qpid.jms.deployment;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.meta.JmsConnectionInfo;
import org.apache.qpid.jms.provider.amqp.AmqpProvider;
import org.apache.qpid.jms.provider.amqp.AmqpProviderFactory;
import org.apache.qpid.jms.provider.failover.FailoverProvider;
import org.apache.qpid.jms.provider.failover.FailoverProviderFactory;
import org.apache.qpid.jms.sasl.AnonymousMechanism;
import org.apache.qpid.jms.sasl.AnonymousMechanismFactory;
import org.apache.qpid.jms.sasl.CramMD5Mechanism;
import org.apache.qpid.jms.sasl.CramMD5MechanismFactory;
import org.apache.qpid.jms.sasl.ExternalMechanism;
import org.apache.qpid.jms.sasl.ExternalMechanismFactory;
import org.apache.qpid.jms.sasl.PlainMechanism;
import org.apache.qpid.jms.sasl.PlainMechanismFactory;
import org.apache.qpid.jms.sasl.ScramSHA1Mechanism;
import org.apache.qpid.jms.sasl.ScramSHA1MechanismFactory;
import org.apache.qpid.jms.sasl.ScramSHA256Mechanism;
import org.apache.qpid.jms.sasl.ScramSHA256MechanismFactory;
import org.apache.qpid.jms.sasl.XOauth2Mechanism;
import org.apache.qpid.jms.sasl.XOauth2MechanismFactory;
import org.apache.qpid.jms.transports.TransportOptions;
import org.apache.qpid.jms.transports.netty.NettySslTransportFactory;
import org.apache.qpid.jms.transports.netty.NettyTcpTransport;
import org.apache.qpid.jms.transports.netty.NettyTcpTransportFactory;
import org.apache.qpid.jms.transports.netty.NettyWsTransport;
import org.apache.qpid.jms.transports.netty.NettyWsTransportFactory;
import org.apache.qpid.jms.transports.netty.NettyWssTransportFactory;
import org.apache.qpid.proton.engine.impl.TransportImpl;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.substrate.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.substrate.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.builditem.substrate.SubstrateResourceBuildItem;

public class QpidJmsProcessor {
    private static final String QPID_JMS = "qpid-jms";

    @BuildStep
    public void enableSecurityServices(BuildProducer<FeatureBuildItem> feature,
                                       BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
        feature.produce(new FeatureBuildItem(QPID_JMS));

        // Indicates desire for the Native SSL support to be enabled. This actually flags the
        // --enable-all-security-services arg for Graal, enabling various other required bits
        // such as Mac tht allows getting some SASL mechs working.
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(QPID_JMS));
    }

    @BuildStep
    public void build(CombinedIndexBuildItem indexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
                      BuildProducer<RuntimeInitializedClassBuildItem> delayedInitialisation,
                      BuildProducer<SubstrateResourceBuildItem> resource) {

        delayedInitialisation.produce(new RuntimeInitializedClassBuildItem(TransportImpl.class.getName()));

        // Provider impls
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqp"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqps"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqpws"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqpwss"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/redirects/ws"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/redirects/wss"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, AmqpProviderFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, AmqpProvider.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/failover"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, FailoverProviderFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, FailoverProvider.class.getName()));

        // Options setter reflective access
        // (See also: AmqpProvider, FailoverProvider, transports, and related factories).
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, JmsConnectionFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, JmsConnectionInfo.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, TransportOptions.class.getName()));

        // Transport impls
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/tcp"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/ssl"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, NettyTcpTransportFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, NettySslTransportFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, NettyTcpTransport.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/ws"));
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/wss"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, NettyWsTransportFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, NettyWssTransportFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, NettyWsTransport.class.getName()));

        // SASL mechanisms
        // NOTE: The [S]CRAM mechs require all security services to be enabled. See #enableSecurityServices at top.
        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/PLAIN"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, PlainMechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, PlainMechanism.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/ANONYMOUS"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, AnonymousMechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, AnonymousMechanism.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/EXTERNAL"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, ExternalMechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, ExternalMechanism.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/CRAM-MD5"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, CramMD5MechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, CramMD5Mechanism.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/SCRAM-SHA-1"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, ScramSHA1MechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, ScramSHA1Mechanism.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/SCRAM-SHA-256"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, ScramSHA256MechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, ScramSHA256Mechanism.class.getName()));

        resource.produce(new SubstrateResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/XOAUTH2"));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, XOauth2MechanismFactory.class.getName()));
        reflectiveClass.produce(new ReflectiveClassBuildItem(true, false, XOauth2Mechanism.class.getName()));
    }
}
