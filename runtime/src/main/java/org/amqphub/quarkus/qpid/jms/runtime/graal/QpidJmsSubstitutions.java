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
package org.amqphub.quarkus.qpid.jms.runtime.graal;

import java.util.concurrent.ThreadFactory;

import org.apache.qpid.jms.transports.TransportOptions;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

class QpidJmsSubstitutions {
}

@TargetClass(className = "org.apache.qpid.jms.transports.TransportSupport")
final class Target_org_apache_qpid_jms_transports_TransportSupport {

    @Substitute
    public static boolean isOpenSSLPossible(TransportOptions options) {
        // Disable OpenSSL support
        return false;
    }
}

@TargetClass(className = "org.apache.qpid.jms.transports.netty.KQueueSupport")
final class Target_org_apache_qpid_jms_transports_netty_KQueueSupport {

    @Substitute
    public static boolean isAvailable(TransportOptions transportOptions) {
        // Disable KQueue support
        return false;
    }

    @Substitute
    public static EventLoopGroup createGroup(int nThreads, ThreadFactory ioThreadfactory) {
        // Wont be called but prevents it initialising the KQueue bits, which fails.
        return new NioEventLoopGroup(1, ioThreadfactory);
    }

    @Substitute
    public static void createChannel(Bootstrap bootstrap) {
        // Wont be called but prevents it initialising the KQueue bits, which fails.
        bootstrap.channel(NioSocketChannel.class);
    }
}

@TargetClass(className = "org.apache.qpid.jms.transports.netty.EpollSupport")
final class Target_org_apache_qpid_jms_transports_netty_EpollSupport {

    @Substitute
    public static boolean isAvailable(TransportOptions transportOptions) {
        // Disable Epoll support
        return false;
    }

    @Substitute
    public static EventLoopGroup createGroup(int nThreads, ThreadFactory ioThreadfactory) {
        // Wont be called but prevents it initialising the Epoll bits, which fails.
        return new NioEventLoopGroup(1, ioThreadfactory);
    }

    @Substitute
    public static void createChannel(Bootstrap bootstrap) {
        // Wont be called but prevents it initialising the Epoll bits, which fails.
        bootstrap.channel(NioSocketChannel.class);
    }
}