/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.cisco.ctao.sparkbot.application;


import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.application.helloworld.rev700101.RunInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sparkbot.application.helloworld.rev700101.SparkbotHelloWorldService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** The Provider class for the Sparkbot app used to hook it up to the ODL
 *  MD-SAL infrastructure.
 * @author jmedved
 *
 */
public class SparkbotAppProvider implements SparkbotHelloWorldService {

    private static final Logger LOG = LoggerFactory.getLogger(SparkbotAppProvider.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration<SparkbotHelloWorldService> rpcReg;
    private final SparkbotApiExamples helloWorld = new SparkbotApiExamples();

    /** Constructor.
      * @param rpcProviderRegistry: reference to the MD-SAL RPC registry.
    */
    public SparkbotAppProvider(final RpcProviderRegistry rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        LOG.info("SparkbotAppProvider constructed");
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        rpcReg = rpcProviderRegistry.addRpcImplementation(SparkbotHelloWorldService.class, this);
        LOG.info("SparkbotAppProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        rpcReg.close();
        LOG.info("SparkbotAppProvider Closed");
    }

    @Override
    public Future<RpcResult<Void>> run(RunInput input) {
        if (input != null) {
            helloWorld.run(input.getAccessToken());
        } else {
            LOG.info("access token not specified");
            helloWorld.run(null);
        }
        return Futures.immediateFuture( RpcResultBuilder.<Void>success().build() );
    }
}