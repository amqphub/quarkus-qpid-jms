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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;

import org.amqphub.quarkus.qpid.jms.runtime.ConnectionFactoryWrapper;
import org.amqphub.quarkus.qpid.jms.runtime.QpidJmsProducer;
import org.amqphub.quarkus.qpid.jms.runtime.QpidJmsRecorder;

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
import org.apache.qpid.jms.tracing.JmsNoOpTracer;
import org.apache.qpid.jms.tracing.JmsNoOpTracerFactory;
import org.apache.qpid.jms.tracing.opentracing.OpenTracingTracer;
import org.apache.qpid.jms.tracing.opentracing.OpenTracingTracerFactory;
import org.apache.qpid.jms.transports.TransportOptions;
import org.apache.qpid.jms.transports.netty.NettySslTransportFactory;
import org.apache.qpid.jms.transports.netty.NettyTcpTransport;
import org.apache.qpid.jms.transports.netty.NettyTcpTransportFactory;
import org.apache.qpid.jms.transports.netty.NettyWsTransport;
import org.apache.qpid.jms.transports.netty.NettyWsTransportFactory;
import org.apache.qpid.jms.transports.netty.NettyWssTransportFactory;
import org.apache.qpid.proton.engine.impl.TransportImpl;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.jms.spi.deployment.ConnectionFactoryWrapperBuildItem;

import java.util.Optional;
import java.util.function.Function;

public class QpidJmsProcessor {
    private static final String QPID_JMS = "qpid-jms";

    private static final String GLOBAL_TRACER_CLASS = "io.opentracing.util.GlobalTracer";

    @BuildStep
    public void enableSecurityServices(BuildProducer<FeatureBuildItem> feature,
                                       BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
        feature.produce(new FeatureBuildItem(QPID_JMS));

        // Indicates desire for the Native SSL support to be enabled. This actually flags the
        // --enable-all-security-services arg for Graal, enabling various other required bits
        // such as Mac that also allows getting some SASL mechanisms working below.
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(QPID_JMS));
    }

    @BuildStep
    AdditionalBeanBuildItem registerBean() {
        return AdditionalBeanBuildItem.unremovableOf(QpidJmsProducer.class);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void connectionFactoryWrapper(Optional<ConnectionFactoryWrapperBuildItem> connectionFactoryWrapper,
                                  QpidJmsRecorder recorder,
                                  BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {
        if (connectionFactoryWrapper.isPresent()) {
            Function<ConnectionFactory, Object> wrapper = connectionFactoryWrapper.get().getWrapper();
            SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem.configure(ConnectionFactoryWrapper.class)
                    .setRuntimeInit()
                    .defaultBean()
                    .unremovable()
                    .scope(ApplicationScoped.class)
                    .runtimeValue(recorder.getConnectionFactoryWrapper(wrapper));
            syntheticBeanProducer.produce(configurator.done());
        }
    }

    @BuildStep
    public void build(CombinedIndexBuildItem indexBuildItem, BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
                      BuildProducer<RuntimeInitializedClassBuildItem> delayedInitialisation,
                      BuildProducer<NativeImageResourceBuildItem> resource) {

        // Delay initialisation of proton-j transport to allow enabling protocol trace with PN_TRACE_FRM env variable
        delayedInitialisation.produce(new RuntimeInitializedClassBuildItem(TransportImpl.class.getName()));

        // Provider impls
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqp"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqps"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqpws"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/amqpwss"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/redirects/ws"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/redirects/wss"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(AmqpProviderFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(AmqpProvider.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/provider/failover"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(FailoverProviderFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(FailoverProvider.class).methods().build());

        // Options setter reflective access
        // (See also: AmqpProvider, FailoverProvider, transports, and related factories).
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(JmsConnectionFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(JmsConnectionInfo.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(TransportOptions.class).methods().build());

        // Transport impls
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/tcp"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/ssl"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(NettyTcpTransportFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(NettySslTransportFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(NettyTcpTransport.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/ws"));
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/transports/wss"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(NettyWsTransportFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(NettyWssTransportFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(NettyWsTransport.class).methods().build());

        // SASL mechanisms
        // NOTE: The [S]CRAM mechs require all security services to be enabled. See #enableSecurityServices at top.
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/PLAIN"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(PlainMechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(PlainMechanism.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/ANONYMOUS"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(AnonymousMechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(AnonymousMechanism.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/EXTERNAL"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(ExternalMechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(ExternalMechanism.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/CRAM-MD5"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(CramMD5MechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(CramMD5Mechanism.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/SCRAM-SHA-1"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(ScramSHA1MechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(ScramSHA1Mechanism.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/SCRAM-SHA-256"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(ScramSHA256MechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(ScramSHA256Mechanism.class).methods().build());

        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/sasl/XOAUTH2"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(XOauth2MechanismFactory.class).methods().build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(XOauth2Mechanism.class).methods().build());

        // Tracing
        resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/tracing/noop"));
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(JmsNoOpTracerFactory.class).build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(JmsNoOpTracer.class).build());

        if (QuarkusClassLoader.isClassPresentAtRuntime(GLOBAL_TRACER_CLASS)) {
            resource.produce(new NativeImageResourceBuildItem("META-INF/services/org/apache/qpid/jms/tracing/opentracing"));
            reflectiveClass.produce(ReflectiveClassBuildItem.builder(OpenTracingTracerFactory.class).build());
            reflectiveClass.produce(ReflectiveClassBuildItem.builder(OpenTracingTracer.class).build());
        }
    }
}
