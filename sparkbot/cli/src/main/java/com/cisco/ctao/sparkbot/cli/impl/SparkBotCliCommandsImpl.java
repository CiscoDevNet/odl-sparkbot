/*
 * Copyright © 2016 Cisco Systems, Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.cisco.ctao.sparkbot.cli.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cisco.ctao.sparkbot.cli.api.SparkBotCliCommands;

public class SparkBotCliCommandsImpl implements SparkBotCliCommands {

    private static final Logger LOG = LoggerFactory.getLogger(SparkBotCliCommandsImpl.class);
    private final DataBroker dataBroker;

    public SparkBotCliCommandsImpl(final DataBroker db) {
        this.dataBroker = db;
        LOG.info("SparkBotCliCommandImpl initialized");
    }

    @Override
    public Object testCommand(Object testArgument) {
        return "This is a test implementation of test-command";
    }
}