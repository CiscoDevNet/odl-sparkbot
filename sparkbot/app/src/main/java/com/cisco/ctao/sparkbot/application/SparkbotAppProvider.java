/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.application;



import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** The Provider class for the Sparkbot app used to hook it up to the ODL
 *  MD-SAL infrastructure.
 * @author jmedved
 *
 */
public class SparkbotAppProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SparkbotAppProvider.class);
    private final RpcProviderRegistry rpcProviderRegistry;

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
        LOG.info("SparkbotAppProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("SparkbotAppProvider Closed");
    }

}